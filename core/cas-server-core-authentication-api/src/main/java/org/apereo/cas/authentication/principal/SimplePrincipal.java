package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
@ToString
@Getter
@NoArgsConstructor
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
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, List<Object>> attributes = new HashMap<>(0);

    /**
     * Instantiates a new simple principal.
     *
     * @param id         the id
     * @param attributes the attributes
     */
    @JsonCreator
    protected SimplePrincipal(@JsonProperty("id") final @NonNull String id,
                              @JsonProperty("attributes") final Map<String, List<Object>> attributes) {
        this.id = id;
        this.attributes = Objects.requireNonNullElseGet(attributes, HashMap::new);
    }

    /**
     * @return An immutable map of principal attributes
     */
    @Override
    public Map<String, List<Object>> getAttributes() {
        val attrs = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        attrs.putAll(this.attributes);
        return attrs;
    }

    @Override
    public int hashCode() {
        val builder = new HashCodeBuilder(83, 31);
        builder.append(this.id.toLowerCase());
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimplePrincipal)) {
            return false;
        }
        val rhs = (SimplePrincipal) obj;
        return StringUtils.equalsIgnoreCase(this.id, rhs.getId());
    }
}
