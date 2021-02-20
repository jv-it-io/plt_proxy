package be.fgov.famhp.autocontrol.pharmacy.proxy.domain;

import be.fgov.fagg.common.VersionedEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Document(collection = "autocontrol_event")
public class AutocontrolEvent extends VersionedEntity implements Serializable {

    @Id
    private String id;

    @NotNull
    @Field("event_id")
    private String eventId;

    @Field("event_time_stamp")
    private LocalDateTime eventTimestamp;

    @Field("event_type")
    private String eventType;

    @JsonProperty("event_self")
    private String self;

    @Field("dossier_id")
    private String dossierId;

    @Field("dossier_href")
    private String dossierHref;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getDossierId() {
        return dossierId;
    }

    public void setDossierId(String dossierId) {
        this.dossierId = dossierId;
    }

    public String getDossierHref() {
        return dossierHref;
    }

    public void setDossierHref(String dossierHref) {
        this.dossierHref = dossierHref;
    }
}
