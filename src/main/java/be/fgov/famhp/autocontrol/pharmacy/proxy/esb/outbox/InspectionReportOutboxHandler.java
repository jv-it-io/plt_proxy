package be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox;

import be.fgov.fagg.common.command.CommandService;
import be.fgov.fagg.common.javers.JaversTypeEnum;
import be.fgov.fagg.common.outbox.subscriber.JaversOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.FileManager;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.DossierControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.AlertDto.ReasonEnum;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.DossierDto;
import be.fgov.famhp.plato.backoffice.domain.InspectionReport;
import be.fgov.famhp.plato.backoffice.domain.enumeration.InspectionType;
import be.fgov.famhp.plato.backoffice.domain.enumeration.LanguageEnum;
import be.fgov.famhp.plato.backoffice.domain.enumeration.ReportStatus;
import be.fgov.famhp.plato.backoffice.domain.view.PlatoDocumentViews;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InspectionReportOutboxHandler extends DocumentOutboxHandler implements JaversOutboxHandler<InspectionReport> {

    private static final Logger log = LoggerFactory.getLogger(InspectionReportOutboxHandler.class);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final String EMAIL_CAPA_SUBJECT_KEY = "email.autoctrl.addedCapa.subject";

    private static final String EMAIL_CAPA_BODY_KEY = "email.autoctrl.addedCapa.body";

    @Autowired
    DossierControllerApi dossierControllerApi;

    private final CommandService commandService;

    private final FileManager inspectionReportFileManager;

    public InspectionReportOutboxHandler(CommandService commandService, FileManager inspectionReportFileManager) {
        this.commandService = commandService;
        this.inspectionReportFileManager = inspectionReportFileManager;
    }

    @Override
    public void execute(InspectionReport doc, List<String> list, JaversTypeEnum javersTypeEnum) {
        log.info("Message send report to autocontrol for inspection" + doc.getInspectionId());
        if(doc.getStatus() != ReportStatus.COMPLETED) {
            log.info("Can't send report : Report should be complete");
            return;
        }
        try {
            //send inspection report
            File file = getFileFromGridFs(doc.getGridfsId());

            if(file != null){
                //create inspection in autoctrl
                createInspectionInAutocontrol(doc);
                //Send inspection report to autocontrol
                sendDocumentToAutocontrol(file, doc.getApbNbr(), doc.getFileLanguage()
                    ,doc.getInspectionId());
                //send initial capa
                sendBaseCapaToAutocontrol(doc);
            }else{
                log.error("File Inspection Report not found in gridfs with id " + doc.getGridfsId() +
                    " for inspection " + doc.getInspectionId() );
            }

        } catch (IOException ioe) {
            log.error("error while reading GridFS file resource");
        } catch (RestClientException rce) {
            log.error("error while calling rest endpoint: " + rce.getMessage());
        }
    }

    @Override
    protected void sendFile(File file, String[] apiParams) throws RestClientException {
        Long dossierId = Long.parseLong(apiParams[0]);
        Long inspectionId = Long.parseLong(apiParams[1]);
        dossierControllerApi.addInspectionReportUsingPOST(dossierId, file, inspectionId);
    }

    @Override
    protected void sendAlert(String apbNbr, String dossierId) throws RestClientException {
        notificationService.sendAlert(apbNbr, Long.parseLong(dossierId), ReasonEnum.REPORT_ADDED);
    }

    @Override
    protected void sendEmail(String apbNbr, LanguageEnum language) throws RestClientException {}

    protected boolean isInspectionDocument() { return true; }

    private void createInspectionInAutocontrol(InspectionReport doc) throws RestClientException {
        DossierDto dossier = getAutocontrolDossier(doc.getApbNbr());
        Long dossierId = dossier.getId();
        //
        Map<String, String> params = new HashMap<>();
        params.put("inspectionTimestamp", doc.getDate().atStartOfDay().format(dateTimeFormatter));
        params.put("inspectionNumber", doc.getInspectionId());
        params.put("type", InspectionType.INSPECTION.name());

        dossierControllerApi.createInspectionUsingPOST(dossierId, params);
    }

    private void sendBaseCapaToAutocontrol(InspectionReport doc) throws JsonProcessingException, RestClientException {
        String[] apiParams = getApiCallParams(doc.getApbNbr(), doc.getInspectionId());
        Long dossierId = Long.parseLong(apiParams[0]);
        Long inspectionId = Long.parseLong(apiParams[1]);

        //limit report doc content to capa fields, via json view
        String capaJson = getObjectMapper().writerWithView(PlatoDocumentViews.Capa.class)
            .writeValueAsString(doc);

        dossierControllerApi.addInpsectionCapaUsingPOST(capaJson, dossierId, inspectionId);

        notificationService.sendAlert(doc.getApbNbr(), dossierId, ReasonEnum.LINKED_CAPA_ADDED);
        notificationService.sendEmail(doc.getApbNbr(), doc.getFileLanguage(), EMAIL_CAPA_SUBJECT_KEY, EMAIL_CAPA_BODY_KEY);
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        mapper.registerModule(new JavaTimeModule()); //handle date
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //write date as string
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

        return mapper;
    }

    @Override
    protected File getFileFromGridFs(String gridFsId) throws IOException {
        Optional<GridFsResource> documentResourceOpt = this.inspectionReportFileManager.getFile(gridFsId, InspectionReport.class);
        return getFileFromResource(documentResourceOpt);
    }
}
