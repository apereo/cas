package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Default MFA Trigger selection strategy.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultMultifactorAuthenticationTriggerSelectionStrategy implements MultifactorAuthenticationTriggerSelectionStrategy {
    private final Collection<MultifactorAuthenticationTrigger> multifactorAuthenticationTriggers;

    @Override
    public Optional<MultifactorAuthenticationProvider> resolve(final HttpServletRequest request,
                                                               final HttpServletResponse response,
                                                               final RegisteredService registeredService,
                                                               final Authentication authentication,
                                                               final Service service) throws Throwable {
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
