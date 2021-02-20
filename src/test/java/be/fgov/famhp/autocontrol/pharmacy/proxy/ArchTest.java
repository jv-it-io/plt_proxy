package be.fgov.famhp.autocontrol.pharmacy.proxy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("be.fgov.famhp.autocontrol.pharmacy.proxy");

        noClasses()
            .that()
            .resideInAnyPackage("be.fgov.famhp.autocontrol.pharmacy.proxy.service..")
            .or()
            .resideInAnyPackage("be.fgov.famhp.autocontrol.pharmacy.proxy.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..be.fgov.famhp.autocontrol.pharmacy.proxy.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
