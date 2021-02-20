package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.mapper;

import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.FormDetailDto;
import be.fgov.famhp.plato.backoffice.domain.form.Answer;
import be.fgov.famhp.plato.backoffice.domain.form.Form;
import be.fgov.famhp.plato.backoffice.domain.form.FormType;
import be.fgov.famhp.plato.backoffice.domain.form.Question;
import be.fgov.famhp.plato.backoffice.domain.form.TranslatedLabel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FormMapper {
    private final AcUserMapper acUserMapper;

    public FormMapper(AcUserMapper acUserMapper) {
        this.acUserMapper = acUserMapper;
    }

    public Form formDetailDtoToForm(FormDetailDto detailDto) {
        Form form = new Form();
        form.setIdAutocontrol(detailDto.getId().toString());
        form.setDateCreated(detailDto.getCreationTimestamp().toDate());
        form.setCreator(acUserMapper.dtoToAutocontrolUser(detailDto.getAuthor()));
        form.setFormTypeVersion((Integer.parseInt(detailDto.getVersion())));
        form.setParsedResponseForm(getAllResponses(detailDto.getCodeLabels(), detailDto.getResponses()));
        return form;
    }

    private FormType getAllResponses(Map<String, Object> codeLabels, Map<String, Object> responses) {
        FormType formType = new FormType();
        List<Question> questions = new ArrayList<>();
        responses.forEach((key, value) -> {
            Question question = new Question();
            question.setLabel(getLabel((Map<String, String>) codeLabels.get(key)));
            List<Answer> answers = new ArrayList<>();
            Answer answer = new Answer();
            answer.setLabel(getLabel((Map<String, String>) codeLabels.get(value)));
            answers.add(answer);
            question.setAnswers(answers);
            questions.add(question);
        });
        formType.setQuestions(questions);
        return formType;
    }

    private TranslatedLabel getLabel(Map<String, String> labels) {
        TranslatedLabel label = new TranslatedLabel();
        label.setEn(labels.get("en"));
        label.setFr(labels.get("fr"));
        label.setNl(labels.get("nl"));
        return label;
    }
}
