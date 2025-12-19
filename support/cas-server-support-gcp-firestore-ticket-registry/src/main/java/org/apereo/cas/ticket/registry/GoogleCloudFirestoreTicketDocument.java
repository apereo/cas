package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.spring.data.firestore.mapping.UpdateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link GoogleCloudFirestoreTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Document
@NoArgsConstructor
@SuperBuilder
@Getter
public class GoogleCloudFirestoreTicketDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -2290923155282530119L;

    @UpdateTime(version = true)
    private Timestamp updateTime;

    @PropertyName("json")
    private String json;

    @PropertyName("prefix")
    private String prefix;

    @DocumentId
    private String ticketId;

    @JsonProperty
    @PropertyName("type")
    private String type;

    @JsonProperty
    @PropertyName("service")
    private String service;

    @JsonProperty
    @PropertyName("principal")
    private String principal;

    @JsonProperty
    @PropertyName("attributes")
    private Map<String, ?> attributes;

    @JsonProperty
    @PropertyName("expireAt")
    private Date expireAt;

    /**
     * Map of fields that can be updated by the registry.
     *
     * @return the map
     */
    public Map<String, Object> asUpdatableMap() {
        return CollectionUtils.wrap(
            "service", this.service,
            "json", this.json,
            "principal", this.principal,
            "attributes", this.attributes);
    }
}
