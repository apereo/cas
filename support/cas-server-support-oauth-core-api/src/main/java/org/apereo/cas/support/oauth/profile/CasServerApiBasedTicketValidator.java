package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;

import java.util.Map;

/**
 * This is a ticket validator for pac4j client that uses CAS back channels to validate ST.
 *
 * @author Kirill Gagarski
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class CasServerApiBasedTicketValidator implements TicketValidator {
    private final CentralAuthenticationService centralAuthenticationService;
    private final ServiceFactory webApplicationServiceFactory;

    @Override
    public Assertion validate(final String ticketId, final String service) {
        val webApplicationService = webApplicationServiceFactory.createService(service);
        val assertion = centralAuthenticationService.validateServiceTicket(ticketId, webApplicationService);
        val authn = assertion.getPrimaryAuthentication();
        val principal = authn.getPrincipal();
        val attrPrincipal = new AttributePrincipalImpl(principal.getId(), (Map) principal.getAttributes());
        return new AssertionImpl(attrPrincipal, (Map) authn.getAttributes());
    }
}
