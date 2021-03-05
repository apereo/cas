package org.apereo.cas.support.openid.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.authentication.principal.OpenIdCredential;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@Deprecated(since = "6.2.0")
@Tag("Authentication")
public class OpenIdCredentialsAuthenticationHandlerTests extends AbstractOpenIdTests {

    private static final String TGT_ID = "test";
    private static final String USERNAME = "test";

    @Autowired
    @Qualifier("openIdCredentialsAuthenticationHandler")
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Test
    public void verifySupports() {
        assertTrue(this.openIdCredentialsAuthenticationHandler.supports(new OpenIdCredential(TGT_ID, USERNAME)));
        assertFalse(this.openIdCredentialsAuthenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    @SneakyThrows
    public void verifyTGTWithSameId() {
        val c = new OpenIdCredential(TGT_ID, USERNAME);
        val t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        assertEquals(TGT_ID, this.openIdCredentialsAuthenticationHandler.authenticate(c).getPrincipal().getId());
    }

    @Test
    public void verifyTGTThatIsExpired() {
        val c = new OpenIdCredential(TGT_ID, USERNAME);
        val t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);
        t.markTicketExpired();
        this.ticketRegistry.updateTicket(t);
        assertThrows(FailedLoginException.class, () -> this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }

    @Test
    public void verifyTGTWithDifferentId() {
        val c = new OpenIdCredential(TGT_ID, "test1");
        val t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        assertThrows(FailedLoginException.class, () -> this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }

    private static TicketGrantingTicket getTicketGrantingTicket() {
        return new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), new HardTimeoutExpirationPolicy(10));
    }
}
