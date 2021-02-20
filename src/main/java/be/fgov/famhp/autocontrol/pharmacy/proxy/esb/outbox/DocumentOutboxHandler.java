package be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox;

import be.fgov.fagg.common.VersionedEntity;
import be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository.BackOfficePlatoDocumentService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.FileManager;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.NotificationService;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.DossierControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.DossierDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.InspectionDetailDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.OrganizationDto;
import be.fgov.famhp.plato.backoffice.domain.enumeration.LanguageEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.client.RestClientException;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DocumentOutboxHandler {

    private final Logger log = LoggerFactory.getLogger(DocumentOutboxHandler.class);

    protected BackOfficePlatoDocumentService backOfficePlatoDocumentService;

    protected DossierControllerApi dossierControllerApi;

    protected NotificationService notificationService;


    @Autowired
    public final void setBackOfficePlatoDocumentService(BackOfficePlatoDocumentService backOfficePlatoDocumentService) {
        this.backOfficePlatoDocumentService = backOfficePlatoDocumentService;
    }

    @Autowired
    public final void setDossierControllerApi(DossierControllerApi dossierControllerApi) {
        this.dossierControllerApi = dossierControllerApi;
    }

    @Autowired
    public final void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    public final void sendDocumentToAutocontrol(File file, String apbNbr, LanguageEnum language, String... extraParams) throws IOException, RestClientException {
//        File file = getFileFromGridFs(fileRef);
        log.info("Send document to autocontrol" + file.getName());
        String[] apiCallParams = getApiCallParams(apbNbr, extraParams);
        // for dossierDocs params[0]=dossierId & for inspectionDocs params[0]=dossierId and params[1]=inspectionId
        sendFile(file, apiCallParams);
        sendAlert(apbNbr, apiCallParams[0]);
        sendEmail(apbNbr, language);
        //hook()
    }

    protected String[] getApiCallParams(String apbNbr, String... extraParams) {
        DossierDto dossier = getAutocontrolDossier(apbNbr);
        Long dossierId = dossier.getId();
        if (isInspectionDocument()) {
            String platoInspectionId = extraParams[0];
            Long autoctrlInspectionId = getAutocontrolInspection(dossierId, platoInspectionId).getId();
            return Stream.of(String.valueOf(dossierId), String.valueOf(autoctrlInspectionId)).toArray(String[]::new);
        }
        return Stream.of(String.valueOf(dossierId)).toArray(String[]::new);
    }

    public final DossierDto getAutocontrolDossier(String apbNumber) throws RestClientException {
        return dossierControllerApi
            .getDossierByApbIdUsingGET(apbNumber, OrganizationDto.OrganizationIdTypeEnum.APB.toString())
            .getItems().get(0);
    }

    public final InspectionDetailDto getAutocontrolInspection(Long dossierId, String platoInspectionId) throws RestClientException, NotFoundException {
        // TODO replace this by calling the new api endpoint "getInspectionByInspectionNbr"
        List<InspectionDetailDto> dossierInspections = dossierControllerApi
            .getAllInspectionsByDossierUsingGET(dossierId).getItems();

        Optional<InspectionDetailDto> inspection = dossierInspections.stream()
            .filter(item -> platoInspectionId.equals(item.getInspectionNumber()))
            .collect(Collectors.toList()).stream().findFirst();

        // TODO improve exception handling here (check with global exception stuff)
        return inspection.orElseThrow(NotFoundException::new);
    }

    protected File getFileFromResource(Optional<GridFsResource> documentResourceOpt) throws IOException {
        if(documentResourceOpt.isPresent()){
            String tempDir = System.getProperty("java.io.tmpdir");
            GridFsResource documentResource = documentResourceOpt.get();
            InputStream inputStream = documentResource.getInputStream();
            File file = new File(tempDir + "/" + documentResource.getFilename());
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file;
        }
        return null;
    }

    protected abstract void sendFile(File file, String... apiParams);

    protected abstract void sendAlert(String apbNbr, String dossierId);

    protected abstract void sendEmail(String apbNbr, LanguageEnum language);

    protected boolean isInspectionDocument() { return false; }
    protected abstract File getFileFromGridFs (String gridFsId) throws IOException;

}
