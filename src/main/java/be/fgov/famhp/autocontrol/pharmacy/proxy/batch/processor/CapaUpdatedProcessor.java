package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor;

import be.fgov.fagg.common.command.Command;
import be.fgov.fagg.common.command.CommandService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.dto.CapaUpdatedEventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.DossierControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.invoker.ApiClient;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.InspectionDetailDto;
import be.fgov.famhp.plato.backoffice.command.CapaUpdateCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CapaUpdatedProcessor implements ProcessorEventsService {

    private final DossierControllerApi dossierControllerApi;
    private final ApiClient apiClient;
    private final CommandService commandService;

    public CapaUpdatedProcessor(DossierControllerApi dossierControllerApi,
                                ApiClient apiClient,
                                CommandService commandService) {
        this.dossierControllerApi = dossierControllerApi;
        this.apiClient = apiClient;
        this.commandService = commandService;
    }

    private static final Logger log = LoggerFactory.getLogger(CapaUpdatedProcessor.class);

    @Override
    public void processEvent(EventDto eventDto) {
        CapaUpdatedEventDto capaUpdatedEventDto = new CapaUpdatedEventDto(eventDto);
        try {
            log.info("Process event for apb " + capaUpdatedEventDto.getOrganisationAPB() + " update capa for inspection id " + capaUpdatedEventDto.getInspectionId());
            log.info("Basepath api client autocontrol " + apiClient.getBasePath());

            ResponseEntity<String> response = this.dossierControllerApi.getLastCapaJsonUsingGETWithHttpInfo(eventDto.getDossier().getId(), capaUpdatedEventDto.getInspectionId());
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successful get capa for inspection id " + capaUpdatedEventDto.getInspectionId());

                Command command = capaUpdatedEventDto.createCapaUpdateCommand(getJsonPatch(response.getBody()));

                //Workaround (code can be removed after autocontrol adaptation to put inspectionNumber in event)
                String inspectionNumber = getInspectionNumber(capaUpdatedEventDto.getInspectionId(), capaUpdatedEventDto.getDossierId());
                ((CapaUpdateCommand) command).setInspectionNumber(inspectionNumber);
                log.info("CapaUpdatedProcessor process event Workaround - Set to capa update command the inspectionNumber : " + inspectionNumber);
                //end workaround

                log.info("send CapaUpdate command to plato for event id " + eventDto.getEventId() + " : " + command);
                commandService.convertSendAndReceive(command);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            e.getStackTrace();
        }
    }

    private String getJsonPatch(String body) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode metaNode = node.get("meta");
            JsonNode diffNode = metaNode.get("diff");
            return diffNode.toPrettyString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getInspectionNumber(Long inspectionId, Long dossierId) throws Exception {
        ResponseEntity<InspectionDetailDto> response = this.dossierControllerApi.getInspectionDetailUsingGETWithHttpInfo(dossierId, inspectionId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getInspectionNumber();
        }
        throw new Exception("CapaUpdatedProcessor process event Workaround - Inspection Number not found for insoectionId : " + inspectionId);
    }

    @Override
    public Command createCommand(Object... o) {
        return null;
    }
}
