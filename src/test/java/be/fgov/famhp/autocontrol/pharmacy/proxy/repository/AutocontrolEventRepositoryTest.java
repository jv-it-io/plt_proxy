package be.fgov.famhp.autocontrol.pharmacy.proxy.repository;

import be.fgov.famhp.autocontrol.pharmacy.proxy.AutocontrolPharmacyProxyApp;
import be.fgov.famhp.autocontrol.pharmacy.proxy.config.MongoDatabaseConfigurationTest;
import be.fgov.famhp.autocontrol.pharmacy.proxy.config.TestSecurityConfiguration;
import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = {AutocontrolPharmacyProxyApp.class,
    TestSecurityConfiguration.class,
})
@Disabled
public class AutocontrolEventRepositoryTest {


    public static final String EVENT_ID_CREATED = "AAAAA";
    private static final String ID_CREATED = "AAAAA";
    private static final String EVENT_TYPE_CREATED = "AAAAA";
    @Autowired
    AutocontrolEventRepository autocontrolEventRepository;


    @BeforeEach
    public void init(){
        autocontrolEventRepository.deleteAll();
    }

    @Test
    public void getAllTest(){
        AutocontrolEvent autocontrolEvent = new AutocontrolEvent();
        autocontrolEvent.setEventId(EVENT_ID_CREATED);
        autocontrolEvent.setId(ID_CREATED);
        autocontrolEvent.setEventType(EVENT_TYPE_CREATED);

        autocontrolEventRepository.save(autocontrolEvent);

        List<AutocontrolEvent> all = autocontrolEventRepository.findAll();

        assertFalse(all.isEmpty());

    }


}
