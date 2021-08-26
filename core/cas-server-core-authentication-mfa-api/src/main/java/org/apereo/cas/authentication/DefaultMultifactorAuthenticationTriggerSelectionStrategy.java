package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;

/**
 * Default MFA Trigger selection strategy.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultMultifactorAuthenticationTriggerSelectionStrategy implements MultifactorAuthenticationTriggerSelectionStrategy {
    private final Collection<MultifactorAuthenticationTrigger> multifactorAuthenticationTriggers;

    @Override
    public Optional<MultifactorAuthenticationProvider> resolve(final HttpServletRequest request,
                                                               final RegisteredService registeredService,
                                                               final Authentication authentication,
                                                               final Service service) {
        if (registeredService != null && registeredService.getMultifactorPolicy().isBypassEnabled()) {
            LOGGER.debug("Multifactor authentication policy for [{}] will ignore trigger executions", registeredService.getName());
            return Optional.empty();
        }

        for (val trigger : multifactorAuthenticationTriggers) {
            if (!trigger.supports(request, registeredService, authentication, service)) {
                continue;
            }
            val activated = trigger.isActivated(authentication, registeredService, request, service);
            if (activated.isPresent()) {
                return activated;
            }
        }
        return Optional.empty();
    }
}
