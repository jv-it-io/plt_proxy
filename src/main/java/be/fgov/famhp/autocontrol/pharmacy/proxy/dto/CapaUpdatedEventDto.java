package be.fgov.famhp.autocontrol.pharmacy.proxy.dto;

import be.fgov.fagg.common.command.Command;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.plato.backoffice.command.CapaUpdateCommand;

import java.util.LinkedHashMap;

public class CapaUpdatedEventDto extends InpsectionEventDto {

    private static final String DETAILS_INSPECTION = "inspection";
    private static final String DETAILS_INSPECTION_NUMBER = "inspectionNumber";

    public CapaUpdatedEventDto(EventDto eventDto) {
        super(eventDto);
    }

    public String getInspectionNumber() {
        Object inspection = ((LinkedHashMap) (getDetails().get(DETAILS_INSPECTION))).get(DETAILS_INSPECTION_NUMBER);
        if (inspection != null) {
            return inspection.toString();
        }
        return "";
    }

    public Command createCapaUpdateCommand(String jsonPatch) {
        CapaUpdateCommand capaUpdateCommand = new CapaUpdateCommand()
            .inspectionNumber(getInspectionNumber())
            .eventId(String.valueOf(getEventId()))
            .jsonPatch(jsonPatch);
        return capaUpdateCommand;
    }
}
