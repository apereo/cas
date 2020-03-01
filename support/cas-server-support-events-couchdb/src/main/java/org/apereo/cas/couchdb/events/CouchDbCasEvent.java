package org.apereo.cas.couchdb.events;

import org.apereo.cas.support.events.dao.CasEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * This is {@link CouchDbCasEvent}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
public class CouchDbCasEvent extends CasEvent {

    private static final long serialVersionUID = 7707486620134127284L;

    /**
     * CouchDb id.
     */
    @JsonProperty("_id")
    private String cid;

    /**
     * CouchDb revision.
     */
    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbCasEvent(@JsonProperty("_id") final String cid,
                           @JsonProperty("_rev") final String rev,
                           @JsonProperty("id") final long id,
                           @JsonProperty("type") final @NonNull String type,
                           @JsonProperty("principalId") final @NonNull String principalId,
                           @JsonProperty("creationTime") final @NonNull String creationTime,
                           @JsonProperty("properties") final Map<String, String> properties
                           ) {
        super(id, type, principalId, creationTime, properties);
        this.cid = cid;
        this.rev = rev;
    }

    /**
     * Copy constructor.
     * @param event Event to copy from.
     */
    public CouchDbCasEvent(final CasEvent event) {
        super(event.getId(), event.getType(), event.getPrincipalId(), event.getCreationTime(), event.getProperties());
    }
}
