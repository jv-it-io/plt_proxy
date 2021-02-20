package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.writer;


import be.fgov.famhp.autocontrol.pharmacy.proxy.service.AutocontrolEventService;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EventItemWriter implements ItemWriter<EventDto> {

    private static final Logger log = LoggerFactory.getLogger(EventItemWriter.class);

    @Autowired
    AutocontrolEventService autocontrolEventService;

    @Override
    public void write(List<? extends EventDto> list) throws Exception {
        autocontrolEventService.saveAll((List<EventDto>) list);
    }
}
