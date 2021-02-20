package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor;

import be.fgov.fagg.common.command.Command;
import be.fgov.fagg.common.command.CommandService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.utils.form.FormsUtils;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.plato.backoffice.command.NewFormCompletedCommand;
import be.fgov.famhp.plato.backoffice.domain.form.Form;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class NewFormCompletedProcessor implements ProcessorEventsService {
    private final FormsUtils formUtils;
    private final CommandService commandService;

    public NewFormCompletedProcessor(FormsUtils formUtils, CommandService commandService) {
        this.formUtils = formUtils;
        this.commandService = commandService;
    }

    @Override
    public void processEvent(EventDto eventDto) {
        if (eventDto.getEventType().equals("NEW_FORM_COMPLETED")) {
            Form form = formUtils.getFormFromAutocontrol(eventDto);

            Command command = createCommand(form, eventDto.getEventId());
            commandService.convertSendAndReceive(command);
        }
    }

    @Override
    public Command createCommand(Object... o) {
        NewFormCompletedCommand command = new NewFormCompletedCommand();
        command.setId(new ObjectId().toString());
        command.setForm((Form) o[0]);
        command.setEventId(o[1].toString());
        return command;
    }
}
