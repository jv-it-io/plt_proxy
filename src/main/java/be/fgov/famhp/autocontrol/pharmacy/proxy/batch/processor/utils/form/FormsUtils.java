package be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.utils.form;

import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.mapper.FormMapper;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.api.DossierControllerApi;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.invoker.ApiClient;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.FormDetailDto;
import be.fgov.famhp.plato.backoffice.domain.form.Form;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class FormsUtils {
    public static final String DETAILS_ORGANIZATION = "organization";
    public static final String DETAILS_ORGANIZATION_ID = "organizationId";

    private final DossierControllerApi dossierControllerApi;
    private final ApiClient apiClient;
    private final FormMapper formMapper;

    private static final Logger log = LoggerFactory.getLogger(FormsUtils.class);

    public FormsUtils(DossierControllerApi dossierControllerApi, ApiClient apiClient, FormMapper formMapper) {
        this.dossierControllerApi = dossierControllerApi;
        this.apiClient = apiClient;
        this.formMapper = formMapper;
    }

    public Form getFormFromAutocontrol(EventDto eventDto) {
        String apbNumber = getOrganisationAPB(eventDto.getDetails());
        Long formId = getFormId(eventDto.getDetails());
        Long dossierId = eventDto.getDossier().getId();
        log.info("Process event for apb " + apbNumber + " get form with id " + formId);
        log.info("Basepath api client autocontrol " + apiClient.getBasePath());

        FormDetailDto formDetailDto = dossierControllerApi.getFormUsingGET(dossierId, formId);
        Form form = formMapper.formDetailDtoToForm(formDetailDto);
        form.getCreator().getOrganization().setEnterpriseNumber(apbNumber);
        form.setDossierId(dossierId.toString());
        return form;
    }

    private Long getFormId(Object details) {
        LinkedHashMap linkedHashMapDetails = (LinkedHashMap) details;
        String href = ((LinkedHashMap) (linkedHashMapDetails.get("form"))).get("href").toString();
        Long formId = Long.valueOf(StringUtils.substringAfterLast(href, "/"));
        return formId;
    }

    private String getOrganisationAPB(Object details) {
        LinkedHashMap linkedHashMapDetails = (LinkedHashMap) details;
        String organizationId = ((LinkedHashMap) (linkedHashMapDetails.get(DETAILS_ORGANIZATION))).get(DETAILS_ORGANIZATION_ID).toString();
        return organizationId;
    }
}
