package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.mapper;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.AcUserDto;
import be.fgov.famhp.plato.backoffice.domain.form.AutocontrolUser;
import be.fgov.famhp.plato.backoffice.domain.form.Organization;
import be.fgov.famhp.plato.backoffice.domain.form.Person;
import org.springframework.stereotype.Component;

@Component
public class AcUserMapper {
    private final static String ORGANIZATION_TYPE = "PHARMACY";

    public AutocontrolUser dtoToAutocontrolUser(AcUserDto acUserDto) {
        Person person = new Person();
        person.setFirstName(acUserDto.getFirstName());
        person.setLastName(acUserDto.getLastName());
        person.setSsin(acUserDto.getSsin());

        Organization organization = new Organization();
        organization.setOrganizationType(ORGANIZATION_TYPE);

        AutocontrolUser autocontrolUser = new AutocontrolUser();
        autocontrolUser.setPerson(person);
        autocontrolUser.setOrganization(organization);
        return autocontrolUser;
    }
}
