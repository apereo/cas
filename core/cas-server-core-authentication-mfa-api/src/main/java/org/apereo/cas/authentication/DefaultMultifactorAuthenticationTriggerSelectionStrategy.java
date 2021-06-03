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

    /**
     * Finalize trigger activation for.
     *
     * @param provider          the provider
     * @param trigger           the trigger
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param request           the request
     * @param service           the service
     * @return the optional
     */
    protected Optional<MultifactorAuthenticationProvider> finalizeTriggerActivationFor(final MultifactorAuthenticationProvider provider,
                                                                                            final MultifactorAuthenticationTrigger trigger,
                                                                                            final Authentication authentication,
                                                                                            final RegisteredService registeredService,
                                                                                            final HttpServletRequest request,
                                                                                            final Service service) {
        LOGGER.trace("Multifactor authentication is triggered via [{}]", trigger.getName());
        if (registeredService.getMultifactorPolicy().isIgnoreExecution()) {
            LOGGER.debug("Multifactor authentication policy for [{}] will ignore executions for trigger [{}]",
                registeredService.getName(), trigger.getName());
            return Optional.empty();
        }
        return Optional.of(provider);
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> resolve(final HttpServletRequest request,
                                                               final RegisteredService registeredService,
                                                               final Authentication authentication,
                                                               final Service service) {
        for (val trigger : multifactorAuthenticationTriggers) {
            if (!trigger.supports(request, registeredService, authentication, service)) {
                continue;
            }
            val activated = trigger.isActivated(authentication, registeredService, request, service);
            if (activated.isPresent()) {
                return finalizeTriggerActivationFor(activated.get(), trigger, authentication,
                    registeredService, request, service);
            }
        }
        return Optional.empty();
    }
}
