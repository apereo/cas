package org.apereo.cas.couchdb.core;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ektorp.support.CouchDbDocument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CouchDbProfileDocument}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouchDbProfileDocument extends CouchDbDocument implements Principal {

    private static final long serialVersionUID = -986478230300427397L;

    /**
     * Username.
     */
    @JsonProperty
    private String username;

    /**
     * Linkedid used by pac4j.
     */
    @JsonProperty
    private String linkedid;

    /**
     `* Map for storing extra properties when AUP support uses shared database (e.g., user database).
     */
    @JsonAnySetter
    private Map<String, List<Object>> attributes = new LinkedHashMap<>(0);

    @Override
    @JsonAnyGetter
    public Map<String, List<Object>> getAttributes() {
        return attributes;
    }

    /**
     * Gets a single attribute.
     * @param key the attribute key to fetch
     * @return the attribute value
     */
    @JsonIgnore
    public Object getAttribute(final String key) {
        return attributes.get(key);
    }

    /**
     * Sets a single attribute.
     * @param key the attribute key to set
     * @param value the value to be set
     */
    @JsonIgnore
    public void setAttribute(final String key, final Object value) {
        attributes.put(key, CollectionUtils.toCollection(value, ArrayList.class));
    }
}
