package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor;

import be.fgov.fagg.common.command.Command;
import be.fgov.fagg.common.command.CommandService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.utils.form.FormsUtils;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.plato.backoffice.command.FormCopyCompletedCommand;
import be.fgov.famhp.plato.backoffice.domain.form.Form;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class FormCopyCompletedProcessor implements ProcessorEventsService {
    private final FormsUtils formsUtils;
    private final CommandService commandService;

    public FormCopyCompletedProcessor(FormsUtils formsUtils, CommandService commandService) {
        this.formsUtils = formsUtils;
        this.commandService = commandService;
    }

    @Override
    public void processEvent(EventDto eventDto) {
        if (eventDto.getEventType().equals("FORMCOPY_COMPLETED")) {
            Form form = formsUtils.getFormFromAutocontrol(eventDto);

            Command command = createCommand(form, eventDto.getEventId());
            commandService.convertSendAndReceive(command);
        }
    }

    @Override
    public Command createCommand(Object... o) {
        FormCopyCompletedCommand command = new FormCopyCompletedCommand();
        command.setId(new ObjectId().toString());
        command.setForm((Form) o[0]);
        command.setEventId(o[1].toString());
        return command;
    }
}
