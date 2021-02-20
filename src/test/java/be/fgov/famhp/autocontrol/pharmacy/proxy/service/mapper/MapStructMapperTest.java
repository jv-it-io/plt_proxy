package be.fgov.famhp.autocontrol.pharmacy.proxy.service.mapper;

import be.fgov.famhp.autocontrol.pharmacy.proxy.AutocontrolPharmacyProxyApp;
import be.fgov.famhp.autocontrol.pharmacy.proxy.config.TestSecurityConfiguration;
import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.DossierEventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {AutocontrolPharmacyProxyApp.class, TestSecurityConfiguration.class})
@Disabled
public class MapStructMapperTest {


    @Autowired
    MapStructMapper mapper;

    @Test
    public void testMapEventDtoToEvent() {
        Long eventId = 15L;
        Long dossierEventId = 154L;

        DateTime dt = new DateTime(2020, 1, 25, 23, 34, 56, 111);

        DossierEventDto dossierEventDto = new DossierEventDto().id(dossierEventId).href("href");

        EventDto eventDto = new EventDto()
            .eventId(eventId)
            .eventTimestamp(dt)
            .eventType("eventType")
            .dossier(dossierEventDto)
            .self("self");

        AutocontrolEvent result = mapper.mapEventDtoToAutocontrolEvent(eventDto, new AutocontrolEvent());

        assertEquals(String.valueOf(eventId), result.getEventId());
        assertEquals(eventDto.getEventType(), result.getEventType());
        assertEquals(eventDto.getSelf(), result.getSelf());
        assertEquals(String.valueOf(dossierEventId), result.getDossierId());
        assertEquals(dossierEventDto.getHref(), result.getDossierHref());
        assertEquals(eventDto.getEventTimestamp().toLocalDateTime(), result.getEventTimestamp());
    }
}
