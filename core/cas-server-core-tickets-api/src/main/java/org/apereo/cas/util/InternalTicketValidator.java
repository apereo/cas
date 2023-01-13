package org.apereo.cas.util;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketValidator;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This is a ticket validator that uses CAS back channels to validate ST.
 *
 * @author Kirill Gagarski
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class InternalTicketValidator implements TicketValidator {
    private final CentralAuthenticationService centralAuthenticationService;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    private final ServicesManager servicesManager;

    @Override
    public ValidationResult validate(final String ticketId, final String serviceId) {
        val service = webApplicationServiceFactory.createService(serviceId);
        val assertion = centralAuthenticationService.validateServiceTicket(ticketId, service);
        val authentication = assertion.primaryAuthentication();
        val principal = authentication.getPrincipal();
        val registeredService = servicesManager.findServiceBy(service);
        val authenticationAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(
            authentication, assertion, new HashMap<>(0), registeredService);
        val context = CollectionUtils.<String, Serializable>wrap(
            Authentication.class.getName(), assertion.originalAuthentication(),
            Assertion.class.getName(), assertion,
            RegisteredService.class.getName(), registeredService);
        context.putAll(assertion.context());

        return ValidationResult.builder()
            .principal(principal)
            .service(service)
            .attributes(authenticationAttributes)
            .context(context)
            .build();
    }
}
