package be.fgov.famhp.autocontrol.pharmacy.proxy.dto;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;

import java.util.LinkedHashMap;

public class InpsectionEventDto extends BasicEventDto {

    private static final String DETAILS_INSPECTION = "inspection";
    private static final String DETAILS_INSPECTION_HREF = "href";

    public InpsectionEventDto(EventDto eventDto) {
        super(eventDto);
    }

    public Long getInspectionId() {
        String href = ((LinkedHashMap) (getDetails().get(DETAILS_INSPECTION))).get(DETAILS_INSPECTION_HREF).toString();
        String[] args = href.split("/");
        return Long.valueOf(args[args.length - 1]);
    }
}
