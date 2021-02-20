package be.fgov.famhp.autocontrol.pharmacy.proxy.service;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.NotificationControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.AlertDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EmailBody;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EmailDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.OrganizationDto;
import be.fgov.famhp.plato.backoffice.domain.enumeration.LanguageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Locale;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationControllerApi notificationControllerApi;

    @Autowired
    private MessageSource messageSource;


    public void sendAlert(String apbNbr, Long dossierId, AlertDto.ReasonEnum reason) throws RestClientException {
        AlertDto alert = new AlertDto().organization(createOrganizationDto(apbNbr))
            .dossierId(dossierId)
            .reason(reason);

        notificationControllerApi.createAlertUsingPOST(alert);
    }

    public void sendEmail(String apbNbr, LanguageEnum language, String subjectKey, String bodyKey) throws RestClientException {
        EmailBody emailBody = new EmailBody();
        emailBody.subject(getTranslatedText(subjectKey, language))
            .body(getTranslatedText(bodyKey, language));

        EmailDto email = new EmailDto().organization(createOrganizationDto(apbNbr))
            .message(emailBody);

        notificationControllerApi.sendMailUsingPOST(email);
    }

    private OrganizationDto createOrganizationDto(String apbNbr) {
        return new OrganizationDto().organizationType("PHARMACY")
            .organizationIdType(OrganizationDto.OrganizationIdTypeEnum.APB)
            .organizationId(apbNbr);
    }

    public String getTranslatedText(String messageKey, LanguageEnum language) {
        return messageSource.getMessage(
                messageKey, new Object[]{}, Locale.forLanguageTag(language.name())
            );
    }
}

