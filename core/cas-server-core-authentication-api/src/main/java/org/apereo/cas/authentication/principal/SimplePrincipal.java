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

import java.io.Serial;
import java.util.List;
import java.util.Locale;
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
@ToString
@Getter
@NoArgsConstructor
public class SimplePrincipal implements Principal {

    @Serial
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
    private Map<String, List<Object>> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

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
        this.attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.attributes.putAll(attributes);
    }

    @Override
    public int hashCode() {
        val builder = new HashCodeBuilder(83, 31);
        builder.append(id.toLowerCase(Locale.ENGLISH));
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
        if (!(obj instanceof final SimplePrincipal rhs)) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(id, rhs.getId());
    }
}
