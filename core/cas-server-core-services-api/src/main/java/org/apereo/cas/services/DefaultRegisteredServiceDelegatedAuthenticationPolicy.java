package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceDelegatedAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
public class DefaultRegisteredServiceDelegatedAuthenticationPolicy implements RegisteredServiceDelegatedAuthenticationPolicy {

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
        return new EqualsBuilder().append(this.allowedProviders, rhs.allowedProviders).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(allowedProviders).toHashCode();
    }

    @Override
    @JsonIgnore
    public boolean isProviderAllowed(final String provider, final RegisteredService registeredService) {
        if (this.allowedProviders.isEmpty()) {
            LOGGER.warn("Registered service [{}] does not define any authorized/supported delegated authentication providers. " + "It is STRONGLY recommended that you authorize and assign providers to the service definition. " + "While just a warning for now, this behavior will be enforced by CAS in future versions.", registeredService.getName());
            return true;
        }
        return this.allowedProviders.contains(provider);
    }
}
