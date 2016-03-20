package org.jasig.cas.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Defines an mfa authentication method
 * and its relevant properties.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
public final class AuthenticationMethod implements Comparable, Serializable {
    private static final long serialVersionUID = -8960685427442975943L;

    @JsonProperty
    private final Integer rank;

    @JsonProperty
    private final String name;

    /**
     * Instantiates a new Authentication method.
     */
    private AuthenticationMethod() {
        this.rank = null;
        this.name = null;
    }

    /**
     * Instantiates a new Authentication method.
     *
     * @param rank the rank
     * @param name the name
     */
    public AuthenticationMethod(final String name, final Integer rank) {
        this.rank = rank;
        this.name = name;
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
        final AuthenticationMethod rhs = (AuthenticationMethod) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .toHashCode();
    }

    public Integer getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("rank", rank)
                .append("name", name)
                .toString();
    }

    @Override
    public int compareTo(final Object o) {
        final AuthenticationMethod m = (AuthenticationMethod) o;
        return new CompareToBuilder().append(this.name, m.getName()).toComparison();
    }
}
