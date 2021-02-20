package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.GenericTypeResolver;

import java.util.Map;

public class ItemProcessorEvent implements ItemProcessor<EventDto, EventDto> {


    private final Map<String, ProcessorEventsService> referenceServiceMap;

    private static final Logger log = LoggerFactory.getLogger(ItemProcessorEvent.class);

    public ItemProcessorEvent(Map<String, ProcessorEventsService> referenceServiceMap) {
        this.referenceServiceMap = referenceServiceMap;
    }

    @Override
    public EventDto process(EventDto eventDto) {
        try {
            log.debug("Process event dto " + eventDto.getEventId() + " / " +  eventDto.getEventType());
            ProcessorEventsService processorEventsService = referenceServiceMap.get(eventDto.getEventType());
            if (processorEventsService != null) {
                processorEventsService.processEvent(eventDto);
            }else
            {
                log.debug("unknow process type " + eventDto.getEventType());
            }
        } catch (Exception e) {
            throw e;
        }
        return eventDto;

    }
}
