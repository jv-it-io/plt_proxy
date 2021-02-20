package be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox;

import be.fgov.fagg.common.command.CommandService;
import be.fgov.fagg.common.javers.JaversTypeEnum;
import be.fgov.fagg.common.outbox.subscriber.JaversOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.FileManager;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.AlertDto.ReasonEnum;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.DossierDto;
import be.fgov.famhp.plato.backoffice.domain.PharmacyDocument;
import be.fgov.famhp.plato.backoffice.domain.enumeration.DocumentExchangeDirection;
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
import java.util.stream.Stream;

@Service
public class PharmacyDocumentOutboxHandler extends DocumentOutboxHandler implements JaversOutboxHandler<PharmacyDocument> {

    private final Logger log = LoggerFactory.getLogger(PharmacyDocumentOutboxHandler.class);

    private static final String EMAIL_SUBJECT_KEY = "email.autoctrl.addedPharmacyDocument.subject";

    private static final String EMAIL_BODY_KEY = "email.autoctrl.addedPharmacyDocument.body";

    private final CommandService commandService;


    private final FileManager pharmacyDocumentFileManager;


    public PharmacyDocumentOutboxHandler(CommandService commandService, FileManager pharmacyDocumentFileManager) {
        this.commandService = commandService;
        this.pharmacyDocumentFileManager = pharmacyDocumentFileManager;
    }

    @Override
    public void execute(PharmacyDocument doc, List<String> list, JaversTypeEnum javersTypeEnum) {
        if(doc.getDocumentExchangeDirection() == DocumentExchangeDirection.AUTOCONTROL_TO_PLATO) {
            log.info("update command to precessed : " + doc.getCommandId());
            commandService.updateProcessed(doc);
        } else if(doc.getDocumentExchangeDirection() == DocumentExchangeDirection.PLATO_TO_AUTOCONTROL) {
            log.info("Message send document to autocontrol (pharmacy)");
            try {
                File file = getFileFromGridFs(doc.getGridfsId());
                if(file != null){
                    sendDocumentToAutocontrol(file, doc.getApbNbr(), doc.getFileLanguage(),
                        doc.getDescription());
                }else
                {
                    log.error("File Pharmacy Document not found in gridfs with id " + doc.getGridfsId() +
                        " for apb " + doc.getApbNbr() );
                }

            } catch (RestClientException rce) {
                log.error("error while calling autocontrol api!", rce);
            } catch (Exception e) {
                log.error("something went wrong!", e);
            }
        }
    }

    @Override
    public final String[] getApiCallParams(String apbNbr, String... extraParams) {
        DossierDto dossier = getAutocontrolDossier(apbNbr);
        Long dossierId = dossier.getId();
        String fileDescription = extraParams[0];
        return Stream.of(String.valueOf(dossierId), fileDescription).toArray(String[]::new);
    }

    @Override
    protected void sendFile(File file, String[] apiParams) throws RestClientException {
        Long dossierId = Long.parseLong(apiParams[0]);
        String fileDescription = apiParams[1];
        dossierControllerApi.addAnnexUsingPOST(dossierId, file, fileDescription);
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
    protected File getFileFromGridFs(String gridFsId) throws IOException {
        Optional<GridFsResource> documentResourceOpt = this.pharmacyDocumentFileManager.getFile(gridFsId, PharmacyDocument.class);
        return getFileFromResource(documentResourceOpt);
    }
}
