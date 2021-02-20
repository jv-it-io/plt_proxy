package be.fgov.famhp.autocontrol.pharmacy.proxy.repository;

import be.fgov.famhp.autocontrol.pharmacy.proxy.domain.AutocontrolEvent;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
//@JaversSpringDataAuditable
public interface AutocontrolEventRepository extends MongoRepository<AutocontrolEvent, String> {

    AutocontrolEvent findFirstByOrderByEventIdDesc();
}
