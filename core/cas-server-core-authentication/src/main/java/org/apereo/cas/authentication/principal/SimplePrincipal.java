package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple implementation of a {@link Principal} that exposes an unmodifiable
 * map of attributes. The attributes are cached upon construction and
 * will not be updated unless the principal is entirely and newly
 * resolved again.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplePrincipal implements Principal {
    /**
     * Serialization support.
     */
    private static final long serialVersionUID = -1255260750151385796L;

    /**
     * The unique identifier for the principal.
     */
    @JsonProperty
    private String id;

    /**
     * Principal attributes.
     **/
    private Map<String, Object> attributes;

    /**
     * No-arg constructor for serialization support.
     */
    private SimplePrincipal() {
        this.id = null;
        this.attributes = new HashMap<>();
    }

    /**
     * Instantiates a new simple principal.
     *
     * @param id the id
     */
    private SimplePrincipal(final String id) {
        this(id, new HashMap<>());
    }

    /**
     * Instantiates a new simple principal.
     *
     * @param id         the id
     * @param attributes the attributes
     */
    @JsonCreator
    protected SimplePrincipal(@JsonProperty("id") final String id,
                              @JsonProperty("attributes") final Map<String, Object> attributes) {

        Assert.notNull(id, "Principal id cannot be null");

        this.id = id;
        if (attributes == null) {
            this.attributes = new HashMap<>();
        } else {
            this.attributes = attributes;
        }
    }

    /**
     * @return An immutable map of principal attributes
     */
    @Override
    public Map<String, Object> getAttributes() {
        final Map<String, Object> attrs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        attrs.putAll(this.attributes);
        return attrs;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(83, 31);
        builder.append(this.id.toLowerCase());
        return builder.toHashCode();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SimplePrincipal rhs = (SimplePrincipal) obj;
        return StringUtils.equalsIgnoreCase(this.id, rhs.getId());
    }
}
