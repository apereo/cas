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
 * Default MFA Trigger selection strategy. This strategy looks for valid triggers in the following order: request
 * parameter, RegisteredService policy, principal attribute.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultMultifactorTriggerSelectionStrategy implements MultifactorAuthenticationTriggerSelectionStrategy {
    private final Collection<MultifactorAuthenticationTrigger> multifactorAuthenticationTriggers;

    @Override
    public Optional<String> resolve(final HttpServletRequest request,
                                    final RegisteredService registeredService,
                                    final Authentication authentication,
                                    final Service service) {
        for (val trigger : multifactorAuthenticationTriggers) {
            val activated = trigger.isActivated(authentication, registeredService, request, service);
            if (activated.isPresent()) {
                return Optional.of(activated.get().getId());
            }
        }
        return Optional.empty();
    }
}
