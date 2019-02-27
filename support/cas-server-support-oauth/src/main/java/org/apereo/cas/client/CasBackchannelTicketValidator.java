package org.apereo.cas.client;

import org.apereo.cas.CentralAuthenticationService;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;

import lombok.val;

/**
 * This is a ticket validator for pac4j client that uses CAS back channels to validate ST.
 *
 * @author Kirill Gagarski
 * @since 6.1.0
 */
public class CasBackchannelTicketValidator implements TicketValidator {
    private final CentralAuthenticationService cas;

    public CasBackchannelTicketValidator(final CentralAuthenticationService cas) {
        this.cas = cas;
    }

    @Override
    public Assertion validate(final String ticketId, final String service) {
        val assertion = cas.validateServiceTicket(ticketId, () -> service);
        val principal = assertion.getPrimaryAuthentication().getPrincipal();
        
        return new AssertionImpl(new AttributePrincipalImpl(principal.getId(), principal.getAttributes()));
    }
}
