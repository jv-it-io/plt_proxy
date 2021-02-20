package be.fgov.famhp.autocontrol.pharmacy.proxy.dto;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.bson.Document;

import java.util.LinkedHashMap;
import java.util.Map;

public class BasicEventDto {

    private static final String DETAILS_ORGANIZATION = "organization";
    private static final String DETAILS_ORGANIZATION_ID = "organizationId";

    private EventDto eventDto;

    public BasicEventDto(EventDto eventDto) {
        this.eventDto = eventDto;
    }

    public String getEventType() {
        return eventDto.getEventType();
    }

    public Long getEventId() {
        return eventDto.getEventId();
    }

    public Long getDossierId() {
        return eventDto.getDossier().getId();
    }

    public Map getDetails() {
        return (LinkedHashMap) eventDto.getDetails();
    }

    public String getOrganisationAPB() {
        String organizationId = ((LinkedHashMap) (getDetails().get(DETAILS_ORGANIZATION))).get(DETAILS_ORGANIZATION_ID).toString();
        return organizationId;
    }
}
