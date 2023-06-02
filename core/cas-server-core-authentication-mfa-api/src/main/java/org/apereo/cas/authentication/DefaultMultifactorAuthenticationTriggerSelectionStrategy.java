package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.Optional;

/**
 * Default MFA Trigger selection strategy.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
@Slf4j
public record DefaultMultifactorAuthenticationTriggerSelectionStrategy(Collection<MultifactorAuthenticationTrigger> multifactorAuthenticationTriggers)
    implements MultifactorAuthenticationTriggerSelectionStrategy {
    @Override
    public Optional<MultifactorAuthenticationProvider> resolve(final HttpServletRequest request,
                                                               final HttpServletResponse response,
                                                               final RegisteredService registeredService,
                                                               final Authentication authentication,
                                                               final Service service) {
        if (registeredService != null && registeredService.getMultifactorAuthenticationPolicy().isBypassEnabled()) {
            LOGGER.debug("Multifactor authentication policy for [{}] will ignore trigger executions", registeredService.getName());
            return Optional.empty();
        }

        for (val trigger : multifactorAuthenticationTriggers) {
            if (BeanSupplier.isNotProxy(trigger) && !trigger.supports(request, registeredService, authentication, service)) {
                continue;
            }
            val activated = trigger.isActivated(authentication, registeredService, request, response, service);
            if (activated.isPresent()) {
                return activated;
            }
        }
        return Optional.empty();
    }
}
