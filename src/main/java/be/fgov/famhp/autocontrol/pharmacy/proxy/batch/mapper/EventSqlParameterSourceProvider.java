package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.mapper;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.HashMap;

public class EventSqlParameterSourceProvider implements ItemSqlParameterSourceProvider<EventDto> {

    private static final Logger log = LoggerFactory.getLogger(EventSqlParameterSourceProvider.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public SqlParameterSource createSqlParameterSource(EventDto eventDto) {
        return new MapSqlParameterSource(new HashMap<String, Object>() {

            {
                put("eventId", eventDto.getEventId());
                put("eventTimestamp", eventDto.getEventTimestamp());
                put("eventType", eventDto.getEventType());
                try {
                    put("json", mapper.writeValueAsString(eventDto));
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
            }
        });
    }
}
