package org.apereo.cas.support.openid.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.authentication.principal.OpenIdCredential;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdCredentialsAuthenticationHandlerTests extends AbstractOpenIdTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private OpenIdCredentialsAuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Test
    public void verifySupports() {
        assertTrue(this.openIdCredentialsAuthenticationHandler.supports(new OpenIdCredential("test", "test")));
        assertFalse(this.openIdCredentialsAuthenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyTGTWithSameId() throws Exception {
        final OpenIdCredential c = new OpenIdCredential("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        assertEquals("test", this.openIdCredentialsAuthenticationHandler.authenticate(c).getPrincipal().getId());
    }

    @Test
    public void verifyTGTThatIsExpired() throws Exception {
        final OpenIdCredential c = new OpenIdCredential("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        t.markTicketExpired();

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("TGT is null or expired.");

        this.openIdCredentialsAuthenticationHandler.authenticate(c);
    }

    @Test
    public void verifyTGTWithDifferentId() throws Exception {
        final OpenIdCredential c = new OpenIdCredential("test", "test1");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Principal ID mismatch");

        this.openIdCredentialsAuthenticationHandler.authenticate(c);
    }

    private TicketGrantingTicket getTicketGrantingTicket() {
        return new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
    }
}
