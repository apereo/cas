package org.apereo.cas.client;

import org.apereo.cas.CentralAuthenticationService;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;

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
        final org.apereo.cas.validation.Assertion ass = cas.validateServiceTicket(ticketId, () -> service);

        return new AssertionImpl(new AttributePrincipalImpl(
                ass.getPrimaryAuthentication().getPrincipal().getId(),
                ass.getPrimaryAuthentication().getPrincipal().getAttributes()));
    }
}
