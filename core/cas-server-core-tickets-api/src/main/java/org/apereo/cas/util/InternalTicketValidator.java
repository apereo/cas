package org.apereo.cas.util;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;

import java.util.HashMap;
import java.util.Map;

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
    @SuppressWarnings("unchecked")
    public Assertion validate(final String ticketId, final String serviceId) {
        val service = webApplicationServiceFactory.createService(serviceId);
        val assertion = centralAuthenticationService.validateServiceTicket(ticketId, service);
        val authentication = assertion.getPrimaryAuthentication();
        val principal = authentication.getPrincipal();
        val attrPrincipal = new AttributePrincipalImpl(principal.getId(), (Map) principal.getAttributes());

        val registeredService = servicesManager.findServiceBy(service);
        val authenticationAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(
            authentication, assertion,
            new HashMap<>(0), registeredService);
        return new AssertionImpl(attrPrincipal, (Map) authenticationAttributes);
    }
}
