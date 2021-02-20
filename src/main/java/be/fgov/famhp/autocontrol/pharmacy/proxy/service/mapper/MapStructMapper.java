package be.fgov.famhp.autocontrol.pharmacy.proxy.service.mapper;


import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapStructMapper {


    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "dossierId", source = "dossier.id"),
        @Mapping(target = "dossierHref", source = "dossier.href"),
        @Mapping(target = "eventTimestamp", source = "eventTimestamp")
    })
    AutocontrolEvent mapEventDtoToAutocontrolEvent(EventDto eventDto, @MappingTarget AutocontrolEvent autocontrolEvent);

    List<AutocontrolEvent> mapListEventDtoToListAutocontrolEvent(List<EventDto> listEventDto);

}
