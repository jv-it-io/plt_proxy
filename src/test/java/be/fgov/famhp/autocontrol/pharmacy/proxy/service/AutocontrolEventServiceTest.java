package be.fgov.famhp.autocontrol.pharmacy.proxy.service;

import be.fgov.famhp.autocontrol.pharmacy.proxy.AutocontrolPharmacyProxyApp;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.BatchJobSpringConfig;
import be.fgov.famhp.autocontrol.pharmacy.proxy.config.TestSecurityConfiguration;
import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import be.fgov.famhp.autocontrol.pharmacy.proxy.repository.AutocontrolEventRepository;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.mapper.MapStructMapper;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.EventControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.DossierEventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AutocontrolPharmacyProxyApp.class,
    TestSecurityConfiguration.class
})
@Disabled
public class AutocontrolEventServiceTest {

    @Autowired
     AutocontrolEventService autocontrolEventService;

    @Autowired
     AutocontrolEventRepository autocontrolEventRepository;

    @Autowired
     MapStructMapper mapper;

    @Autowired
    EventControllerApi eventControllerApi;

    @BeforeEach
    public void platoPharmacyServiceTestInit() {
        this.autocontrolEventService = new AutocontrolEventService(eventControllerApi, mapper, autocontrolEventRepository);
        this.autocontrolEventRepository.deleteAll();
    }

    @Test
    public void saveEvent() {
        Long eventId = 15L;
        Long dossierEventId = 154L;

        DateTime dt = new DateTime(2020, 1, 25, 23, 34, 56, 111);

        DossierEventDto dossierEventDto = new DossierEventDto().id(dossierEventId).href("href");

        EventDto eventDto = new EventDto().eventId(eventId).eventTimestamp(dt).eventType("eventType").dossier(dossierEventDto).self("self");

        AutocontrolEvent result = autocontrolEventService.saveEvent(eventDto);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEventId()).isEqualTo(String.valueOf(eventDto.getEventId()));
        assertThat(result.getEventType()).isEqualTo(eventDto.getEventType());
        assertThat(result.getDossierHref()).isEqualTo(eventDto.getDossier().getHref());
        assertThat(result.getDossierId()).isEqualTo(String.valueOf(eventDto.getDossier().getId()));
        assertThat(result.getSelf()).isEqualTo(eventDto.getSelf());
        assertThat(eventDto.getEventTimestamp().toDateTime().equals(result.getEventTimestamp()));


    }
}
