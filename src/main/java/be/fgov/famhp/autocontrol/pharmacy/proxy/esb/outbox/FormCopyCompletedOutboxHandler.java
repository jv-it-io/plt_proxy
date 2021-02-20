package be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox;

import be.fgov.fagg.common.command.CommandService;
import be.fgov.fagg.common.javers.JaversTypeEnum;
import be.fgov.fagg.common.outbox.subscriber.JaversOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.DossierControllerApi;
import be.fgov.famhp.plato.backoffice.domain.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormCopyCompletedOutboxHandler implements JaversOutboxHandler<Form> {
    private final Logger log = LoggerFactory.getLogger(FormCopyCompletedOutboxHandler.class);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final CommandService commandService;

    @Autowired
    DossierControllerApi dossierControllerApi;

    public FormCopyCompletedOutboxHandler(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public void execute(Form form, List<String> list, JaversTypeEnum javersTypeEnum) {
        log.info("Outbox Handler: " + form);
        String expiration = form.getDateExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormatter);
        String expectedConfirmation = form.getDateExpectedConfirmation().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("expirationTimestamp", expiration);
        params.put("expectedConfirmationTimestamp", expectedConfirmation);

        dossierControllerApi.updateFormUsingPATCH(Long.valueOf(form.getDossierId()), Long.valueOf(form.getIdAutocontrol()), params);
    }
}
