package org.apereo.cas.authentication.principal;

import module java.base;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@ToString
@Getter
@NoArgsConstructor
@SuppressWarnings("NullAway.Init")
public class SimplePrincipal implements Principal {

    @Serial
    private static final long serialVersionUID = -1255260750151385796L;

    @JsonProperty
    private String id;

    /**
     * Principal attributes.
     **/
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, List<Object>> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @JsonCreator
    protected SimplePrincipal(
        @JsonProperty("id")
        final String id,
        @JsonProperty("attributes")
        final Map<String, List<Object>> attributes) {
        this.id = id;
        this.attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.attributes.putAll(attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof final SimplePrincipal rhs) {
            return id != null && id.equalsIgnoreCase(rhs.getId());
        }
        return false;
    }

    @Override
    public Principal withAttributes(final Map<String, List<Object>> attributes) {
        return new SimplePrincipal(id, attributes);
    }
}
