package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor;

import be.fgov.fagg.common.command.Command;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;

public interface ProcessorEventsService {

    void processEvent(EventDto eventDto);
    Command createCommand(Object ... o);
}
