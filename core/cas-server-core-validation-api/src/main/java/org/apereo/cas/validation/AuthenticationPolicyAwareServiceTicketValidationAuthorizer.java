package org.apereo.cas.validation;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link AuthenticationPolicyAwareServiceTicketValidationAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationPolicyAwareServiceTicketValidationAuthorizer implements ServiceTicketValidationAuthorizer {
    private final ServicesManager servicesManager;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void authorize(final HttpServletRequest request, final Service service, final Assertion assertion) {
        val registeredService = servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

        LOGGER.debug("Evaluating service [{}] to ensure required authentication handlers can satisfy assertion", service);
        val primaryAuthentication = assertion.getPrimaryAuthentication();
        val attributes = primaryAuthentication.getAttributes();
        if (!attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            LOGGER.warn("No successful authentication handlers are recorded for the authentication attempt");
            throw UnauthorizedServiceException.denied("Unauthorized: %s".formatted(service.getId()));
        }
        val successfulHandlerNames = CollectionUtils.toCollection(attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        val assertedHandlers = authenticationEventExecutionPlan.resolveAuthenticationHandlers()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(handler -> successfulHandlerNames.contains(handler.getName()))
            .collect(Collectors.toSet());

        val policies = authenticationEventExecutionPlan.getAuthenticationPolicies(primaryAuthentication);
        policies.forEach(policy -> {
            try {
                val simpleName = policy.getClass().getSimpleName();
                LOGGER.trace("Executing authentication policy [{}]", simpleName);
                val result = policy.isSatisfiedBy(primaryAuthentication, assertedHandlers, applicationContext,
                    Map.of(Assertion.class.getName(), assertion, RegisteredService.class.getName(), registeredService));
                if (!result.isSuccess()) {
                    throw UnauthorizedServiceException.denied("Unauthorized: %s".formatted(service.getId()));
                }
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
                throw UnauthorizedServiceException.denied("Unauthorized: %s".formatted(service.getId()));
            }
        });
    }
}
