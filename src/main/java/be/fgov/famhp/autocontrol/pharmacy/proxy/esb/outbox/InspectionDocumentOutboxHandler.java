package be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox;

import be.fgov.fagg.common.command.CommandService;
import be.fgov.fagg.common.javers.JaversTypeEnum;
import be.fgov.fagg.common.outbox.subscriber.JaversOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.FileManager;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.AlertDto.ReasonEnum;
import be.fgov.famhp.plato.backoffice.domain.InspectionDocument;
import be.fgov.famhp.plato.backoffice.domain.enumeration.LanguageEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class InspectionDocumentOutboxHandler extends DocumentOutboxHandler implements JaversOutboxHandler<InspectionDocument> {

    private final Logger log = LoggerFactory.getLogger(InspectionDocumentOutboxHandler.class);

    private static final String EMAIL_SUBJECT_KEY = "email.autoctrl.addedInspectionDocument.subject";

    private static final String EMAIL_BODY_KEY = "email.autoctrl.addedInspectionDocument.body";

    private final CommandService commandService;

    private final FileManager inspectionDocumentFileManager;


    public InspectionDocumentOutboxHandler(CommandService commandService, FileManager inspectionDocumentFileManager) {
        this.commandService = commandService;

        this.inspectionDocumentFileManager = inspectionDocumentFileManager;
    }

    @Override
    public void execute(InspectionDocument doc, List<String> list, JaversTypeEnum javersTypeEnum) {
        try {
            log.info("Message send docment to inspection in autocontrol, inspection id: " + doc.getInspectionId());
            File file = getFileFromGridFs(doc.getGridfsId());
            if(file != null){
                sendDocumentToAutocontrol(file, doc.getApbNbr(), doc.getFileLanguage()
                    ,doc.getInspectionId());
            }else{
                log.error("File Inspection Document not found in gridfs with id " + doc.getGridfsId() +
                    " for inspection " + doc.getInspectionId() );
            }

        } catch (RestClientException rce) {
            log.error("error while calling autocontrol api!", rce);
        } catch (Exception e) {
            log.error("something went wrong!", e);
        }
    }

    @Override
    protected void sendFile(File file, String[] apiParams) throws RestClientException {
        Long dossierId = Long.parseLong(apiParams[0]);
        Long inspectionId = Long.parseLong(apiParams[1]);
        dossierControllerApi.addInspectionAnnexUsingPOST(dossierId, file, inspectionId);
    }

    @Override
    protected void sendAlert(String apbNbr, String dossierId) throws RestClientException {
        notificationService.sendAlert(apbNbr, Long.parseLong(dossierId), ReasonEnum.DOCUMENT_ADDED);
    }

    @Override
    protected void sendEmail(String apbNbr, LanguageEnum language) throws RestClientException {
        notificationService.sendEmail(apbNbr, language, EMAIL_SUBJECT_KEY, EMAIL_BODY_KEY);
    }

    @Override
    protected boolean isInspectionDocument() { return true; }

    @Override
    protected File getFileFromGridFs(String gridFsId) throws IOException {
        Optional<GridFsResource> documentResourceOpt = this.inspectionDocumentFileManager.getFile(gridFsId, InspectionDocument.class);
        return getFileFromResource(documentResourceOpt);
    }

}
