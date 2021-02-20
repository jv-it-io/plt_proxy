package be.fgov.famhp.autocontrol.pharmacy.proxy.service;

import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import be.fgov.famhp.autocontrol.pharmacy.proxy.repository.AutocontrolEventRepository;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.mapper.MapStructMapper;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.EventControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventListDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutocontrolEventService {
    private final EventControllerApi eventControllerApi;
    private final MapStructMapper mapper;
    private final AutocontrolEventRepository autocontrolEventRepository;

    public AutocontrolEventService(
        EventControllerApi eventControllerApi,
        MapStructMapper mapStructMapper,
        AutocontrolEventRepository autocontrolEventRepository
    ) {
        this.eventControllerApi = eventControllerApi;
        this.mapper = mapStructMapper;
        this.autocontrolEventRepository = autocontrolEventRepository;
    }

    public EventListDto testGetEvent() {
        EventListDto eventsUsingGET = eventControllerApi.getEventsUsingGET(Integer.toUnsignedLong(0), 1, 10);
        return eventsUsingGET;
    }

    public EventListDto getEvents(Long lastEventId, Integer page, Integer pageSize) {
        EventListDto eventsUsingGET = eventControllerApi.getEventsUsingGET(lastEventId, page, pageSize);
        return eventsUsingGET;
    }

    public AutocontrolEvent saveEvent(EventDto eventDto) {
        AutocontrolEvent autocontrolEvent = new AutocontrolEvent();
        autocontrolEvent = mapper.mapEventDtoToAutocontrolEvent(eventDto, autocontrolEvent);
        return autocontrolEventRepository.save(autocontrolEvent);
    }

    public List<AutocontrolEvent> saveAll(List<EventDto> listEventDto) {
        List<AutocontrolEvent> events = mapper.mapListEventDtoToListAutocontrolEvent(listEventDto);
        return autocontrolEventRepository.saveAll(events);
    }

    public AutocontrolEvent findLastEvent() {
        return autocontrolEventRepository.findFirstByOrderByEventIdDesc();
    }
}
