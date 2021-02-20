package be.fgov.famhp.autocontrol.pharmacy.proxy.domain.backofficemongodb;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A PlatoDocument.
 */
@Document(collection = "plato_document")
public class PlatoDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("inspection_id")
    private String inspectionId;

    @Field("action_id")
    private String actionId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInspectionId() {
        return inspectionId;
    }

    public PlatoDocument inspectionId(String inspectionId) {
        this.inspectionId = inspectionId;
        return this;
    }

    public void setInspectionId(String inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getActionId() {
        return actionId;
    }

    public PlatoDocument actionId(String actionId) {
        this.actionId = actionId;
        return this;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatoDocument)) {
            return false;
        }
        return id != null && id.equals(((PlatoDocument) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PlatoDocument{" +
            "id=" + getId() +
            ", inspectionId='" + getInspectionId() + "'" +
            ", actionId='" + getActionId() + "'" +
            "}";
    }
}
