package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * This is {@link DefaultRegisteredServiceDelegatedAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultRegisteredServiceDelegatedAuthenticationPolicy implements RegisteredServiceDelegatedAuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class);
    private static final long serialVersionUID = -784106970642770923L;

    private Collection<String> allowedProviders;

    public DefaultRegisteredServiceDelegatedAuthenticationPolicy() {
        this(new LinkedHashSet<>());
    }

    public DefaultRegisteredServiceDelegatedAuthenticationPolicy(final Collection<String> allowedProviders) {
        this.allowedProviders = allowedProviders;
    }

    @Override
    public Collection<String> getAllowedProviders() {
        return this.allowedProviders;
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
        final DefaultRegisteredServiceDelegatedAuthenticationPolicy rhs = (DefaultRegisteredServiceDelegatedAuthenticationPolicy) obj;
        return new EqualsBuilder()
            .append(this.allowedProviders, rhs.allowedProviders)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(allowedProviders)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("allowedProviders", allowedProviders)
            .toString();
    }

    @Override
    @JsonIgnore
    public boolean isProviderAllowed(final String provider, final RegisteredService registeredService) {
        if (this.allowedProviders.isEmpty()) {
            LOGGER.warn("Registered service [{}] does not define any authorized/supported delegated authentication providers. "
                + "It is STRONGLY recommended that you authorize and assign providers to the service definition. "
                + "While just a warning for now, this behavior will be enforced by CAS in future versions.", registeredService.getName());
            return true;
        }
        return this.allowedProviders.contains(provider);
    }
}
