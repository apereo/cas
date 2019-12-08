package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedHashSet;

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
@EqualsAndHashCode(exclude = "allowedProviders")
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultRegisteredServiceDelegatedAuthenticationPolicy implements RegisteredServiceDelegatedAuthenticationPolicy {
    private static final long serialVersionUID = -784106970642770923L;

    private Collection<String> allowedProviders = new LinkedHashSet<>(0);

    private boolean permitUndefined = true;

    private boolean exclusive;

    @Override
    @JsonIgnore
    public boolean isProviderAllowed(final String provider, final RegisteredService registeredService) {
        if (getAllowedProviders() == null || getAllowedProviders().isEmpty()) {
            LOGGER.warn("Registered service [{}] does not define any authorized/supported delegated authentication providers. "
                + "It is STRONGLY recommended that you authorize and assign providers to the service definition. "
                + "While just a warning for now, this behavior will be enforced by CAS in future versions.",
                registeredService.getName());
            return this.permitUndefined;
        }
        return getAllowedProviders().contains(provider);
    }
}
