package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor;

import be.fgov.fagg.common.command.Command;
import be.fgov.fagg.common.command.CommandService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository.BackOfficePlatoDocumentService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.util.DateUtil;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.DossierControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.invoker.ApiClient;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.AnnexDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.plato.backoffice.command.AnnexeDocumentAddedCommand;
import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.LinkedHashMap;

@Service
public class AnnexeDocumentAddedProcessor implements ProcessorEventsService {

    public static final String DETAILS_ORGANIZATION = "organization";
    public static final String DETAILS_ORGANIZATION_ID = "organizationId";
    private final DossierControllerApi dossierControllerApi;
    private final ApiClient apiClient;


    private final BackOfficePlatoDocumentService backOfficePlatoDocumentService;

    private final CommandService commandService;

    private static final Logger log = LoggerFactory.getLogger(AnnexeDocumentAddedProcessor.class);

    public AnnexeDocumentAddedProcessor(DossierControllerApi dossierControllerApi, ApiClient apiClient, BackOfficePlatoDocumentService backOfficePlatoDocumentService, CommandService commandService) {
        this.dossierControllerApi = dossierControllerApi;
        this.apiClient = apiClient;
        this.backOfficePlatoDocumentService = backOfficePlatoDocumentService;
        this.commandService = commandService;
    }


    @Override
    public void processEvent(EventDto eventDto) {
        if (eventDto.getEventType().equals("ANNEX_DOCUMENT_ADDED")) {
            try {
                //Use parallel call if no use of details ; i.e :
                //**********************************
//                List<Boolean> resultsChecks = new ArrayList<>();
//                Boolean resultLast = Flux.fromStream(Stream.of(MPM_AUTHORIZATION_SERVICE_NAME,
//                    MPM_ORGANISATION_SERVICE_NAME,
//                    MPM_REFERENCEDATA_SERVICE_NAME,
//                    MPM_RODUCTGROUP_SERVICE_NAME))
//                    .log()
//                    .parallel()
//                    .runOn(Schedulers.elastic())
//                    .flatMap(this::checkMigrationProfile)
//                    .map(resultsChecks::add)
//                    .sequential()
//                    .blockLast();
                //*********************************

                String organisationAPB = getOrganisationAPB(eventDto.getDetails());
                Long annexeId = getAnnexeId(eventDto.getDetails());
                log.info("Process event for apb " + organisationAPB + " add annexe id " + annexeId);
                log.info("Basepath api client autocontrol " + apiClient.getBasePath());
                AnnexDto annexDto = dossierControllerApi.getAnnexMetadataUsingGET(annexeId, eventDto.getDossier().getId());
                ResponseEntity<byte[]> documentsResponseEntity = dossierControllerApi.getAnnexDocumentUsingGETWithHttpInfo(annexeId, eventDto.getDossier().getId());
                if (documentsResponseEntity.getStatusCode().is2xxSuccessful()) {
                    log.info("Successful get annexe for annexe id " + annexeId);

                    MediaType mediaType = documentsResponseEntity.getHeaders().getContentType();
                    String fileName = documentsResponseEntity.getHeaders().getContentDisposition().getFilename();
                    InputStream inputStream = ByteSource.wrap(documentsResponseEntity.getBody()).openStream();
                    ObjectId gridFsId = backOfficePlatoDocumentService.storeInputStream(inputStream, fileName, mediaType.toString());
                    if (!gridFsId.equals(null)) {
                        log.info("Successful store file  " + gridFsId);
                        //Create command in another service using mapper mapstruct ; i.e : by impl the class annexeDocumentAddedCommandCreator (which extends abstract class CommandCreator
                        Command command = createCommand(organisationAPB, fileName, gridFsId, mediaType, eventDto.getEventId(), annexDto.getTitle(), annexDto.getCreationTimestamp());
                        log.info("send command to plato for event id " + eventDto.getEventId() + " : " + command);
                        commandService.convertSendAndReceive(command);
                    }
                } else {
                    throw new Exception("Response Entity Status isn't 200");
                }

            } catch (Exception e) {
                log.error(e.getMessage());
                e.getStackTrace();
            }
        }
    }

    private Long getAnnexeId(Object details) {
        LinkedHashMap linkedHashMapDetails = (LinkedHashMap) details;
        String href = ((LinkedHashMap) (linkedHashMapDetails.get("annex"))).get("href").toString();
        Long annexeId = Long.valueOf(StringUtils.substringAfterLast(href, "/"));
        return annexeId;
    }

    private String getOrganisationAPB(Object details) {
        LinkedHashMap linkedHashMapDetails = (LinkedHashMap) details;
        String organizationId = ((LinkedHashMap) (linkedHashMapDetails.get(DETAILS_ORGANIZATION))).get(DETAILS_ORGANIZATION_ID).toString();
        return organizationId;
    }

    @Override
    public Command createCommand(Object... o) {
        //mapper between annexeDocumentDto and annexeDocumentAddedCommand
        AnnexeDocumentAddedCommand annexeDocumentAddedCommand = new AnnexeDocumentAddedCommand();
        annexeDocumentAddedCommand.setId(new ObjectId().toString());
        annexeDocumentAddedCommand.setApbNbr(o[0].toString());
        annexeDocumentAddedCommand.setFileName(o[1].toString());
        annexeDocumentAddedCommand.setGridFsId(((ObjectId) o[2]).toString());
        MediaType mediaType = (MediaType) o[3];
        annexeDocumentAddedCommand.setMediaType(mediaType.getType());
        annexeDocumentAddedCommand.setEventId(o[4].toString());
        annexeDocumentAddedCommand.setDescription(o[5].toString());
        annexeDocumentAddedCommand.setDate(DateUtil.toLocalDate((DateTime) o[6]));

        return annexeDocumentAddedCommand;
    }

}
