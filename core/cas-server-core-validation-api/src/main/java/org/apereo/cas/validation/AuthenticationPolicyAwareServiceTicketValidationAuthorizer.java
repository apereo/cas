package org.apereo.cas.validation;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.http.HttpServletRequest;

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
        val registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

        LOGGER.debug("Evaluating service [{}] to ensure required authentication handlers can satisfy assertion", service);
        val primaryAuthentication = assertion.getPrimaryAuthentication();
        val attributes = primaryAuthentication.getAttributes();
        if (!attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        val successfulHandlerNames = CollectionUtils.toCollection(attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        val assertedHandlers = authenticationEventExecutionPlan.getAuthenticationHandlers()
            .stream()
            .filter(handler -> successfulHandlerNames.contains(handler.getName()))
            .collect(Collectors.toSet());

        val policies = authenticationEventExecutionPlan.getAuthenticationPolicies(primaryAuthentication);
        policies.forEach(p -> {
            try {
                val simpleName = p.getClass().getSimpleName();
                LOGGER.trace("Executing authentication policy [{}]", simpleName);
                if (!p.isSatisfiedBy(primaryAuthentication, assertedHandlers, applicationContext)) {
                    throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
            }
        });
    }
}
