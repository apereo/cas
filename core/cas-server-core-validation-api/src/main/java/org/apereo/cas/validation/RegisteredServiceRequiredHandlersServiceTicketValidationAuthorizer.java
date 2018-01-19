package org.apereo.cas.validation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@AllArgsConstructor
public class RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer implements ServiceTicketValidationAuthorizer {
    private final ServicesManager servicesManager;

    @Override
    public void authorize(final HttpServletRequest request, final Service service, final Assertion assertion) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

        if (registeredService.getRequiredHandlers() != null && !registeredService.getRequiredHandlers().isEmpty()) {
            LOGGER.debug("Evaluating service [{}] to ensure required authentication handlers can satisfy assertion", service);
            final Map<String, Object> attributes = assertion.getPrimaryAuthentication().getAttributes();
            if (attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
                final Set<Object> assertedHandlers = CollectionUtils.toCollection(
                    attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
                final boolean matchesAll = registeredService.getRequiredHandlers()
                    .stream()
                    .allMatch(assertedHandlers::contains);
                if (!matchesAll) {
                    throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
                }
            }
        }
    }
}
