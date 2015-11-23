package org.jasig.cas.authentication.principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
public final class SimplePrincipal implements Principal {
    /** Serialization support. */
    private static final long serialVersionUID = -1255260750151385796L;

    /** The unique identifier for the principal. */
    private final String id;

    /** Principal attributes. **/
    private final Map<String, Object> attributes;

    /** No-arg constructor for serialization support. */
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
        this(id, Collections.EMPTY_MAP);
    }

    /**
     * Instantiates a new simple principal.
     *
     * @param id the id
     * @param attributes the attributes
     */
    protected SimplePrincipal(final String id, final Map<String, Object> attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    /**
     * @return An immutable map of principal attributes
     */
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(83, 31);
        builder.append(this.id);
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
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }
}
