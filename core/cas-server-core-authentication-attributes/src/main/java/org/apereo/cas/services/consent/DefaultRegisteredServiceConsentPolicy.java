package org.apereo.cas.services.consent;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredServiceConsentPolicy;

import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceConsentPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultRegisteredServiceConsentPolicy implements RegisteredServiceConsentPolicy {
    private static final long serialVersionUID = -2771506941879419063L;
    
    private boolean enabled = true;
    private Set<String> excludedAttributes;
    private Set<String> includeOnlyAttributes;

    public DefaultRegisteredServiceConsentPolicy() {
    }

    public DefaultRegisteredServiceConsentPolicy(final Set<String> excludedAttributes, final Set<String> includeOnlyAttributes) {
        this.excludedAttributes = excludedAttributes;
        this.includeOnlyAttributes = includeOnlyAttributes;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setExcludedAttributes(final Set<String> excludedAttributes) {
        this.excludedAttributes = excludedAttributes;
    }

    public void setIncludeOnlyAttributes(final Set<String> includeOnlyAttributes) {
        this.includeOnlyAttributes = includeOnlyAttributes;
    }

    @Override
    public Set<String> getExcludedAttributes() {
        return this.excludedAttributes;
    }

    @Override
    public Set<String> getIncludeOnlyAttributes() {
        return this.includeOnlyAttributes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("excludedAttributes", excludedAttributes)
                .append("includeOnlyAttributes", includeOnlyAttributes)
                .append("enabled", enabled)
                .toString();
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
        final DefaultRegisteredServiceConsentPolicy rhs = (DefaultRegisteredServiceConsentPolicy) obj;
        return new EqualsBuilder()
                .append(this.excludedAttributes, rhs.excludedAttributes)
                .append(this.includeOnlyAttributes, rhs.includeOnlyAttributes)
                .append(this.enabled, rhs.enabled)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(excludedAttributes)
                .append(includeOnlyAttributes)
                .append(enabled)
                .toHashCode();
    }
}
