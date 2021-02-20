package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.reader;

import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.AutocontrolEventService;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventListDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

public class EventItemReader implements ItemReader<EventDto> {

    private static final Logger log = LoggerFactory.getLogger(EventItemReader.class);

    private int nextEventIndex;
    private EventListDto eventData;

    @Autowired
    AutocontrolEventService autocontrolEventService;

    @Value("${api.autocontrol.pagination.defaultPage}")
    int defaultPage;

    @Value("${api.autocontrol.pagination.defaultPageSize}")
    int defaultPageSize;


    public EventItemReader() {
        nextEventIndex = 0;
    }

    @Override
    public EventDto read() throws Exception {
        if (eventDataIsNotInitialized()) {
            eventData = fetchEventDataFromAPI(getLastFetchedEventId());
        }

        List<EventDto> eventItemList = eventData.getItems().stream().collect(Collectors.toList());

        EventDto nextEvent = null;

        if (nextEventIndex < eventItemList.size()) {
            nextEvent = eventItemList.get(nextEventIndex);
            nextEventIndex++;
        }

        return nextEvent;
    }

    private boolean eventDataIsNotInitialized() {
        return this.eventData == null;
    }

    private EventListDto fetchEventDataFromAPI(Long lastEventId) {
        log.debug("fetching events after " + lastEventId);
        return autocontrolEventService.getEvents(lastEventId, defaultPage, defaultPageSize);
    }

    private Long getLastFetchedEventId() {
        AutocontrolEvent lastEvent = autocontrolEventService.findLastEvent();
        if(lastEvent == null) return 0L;
        return Long.parseLong(lastEvent.getEventId());
    }

}
