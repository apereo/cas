package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.ToString;
import lombok.Getter;

/**
 * This is {@link DefaultRegisteredServiceDelegatedAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class DefaultRegisteredServiceDelegatedAuthenticationPolicy implements RegisteredServiceDelegatedAuthenticationPolicy {
    private static final long serialVersionUID = -784106970642770923L;

    private Collection<String> allowedProviders = new LinkedHashSet<>();

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
