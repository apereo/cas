package org.apereo.cas.support.openid.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.authentication.principal.OpenIdCredential;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@Deprecated(since = "6.2.0")
@Tag("AuthenticationHandler")
public class OpenIdCredentialsAuthenticationHandlerTests extends AbstractOpenIdTests {

    private static final String TGT_ID = "test";
    private static final String USERNAME = "test";

    @Autowired
    @Qualifier("openIdCredentialsAuthenticationHandler")
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    public void verifySupports() {
        assertTrue(openIdCredentialsAuthenticationHandler.supports(new OpenIdCredential(TGT_ID, USERNAME)));
        assertFalse(openIdCredentialsAuthenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyTGTWithSameId() throws Exception {
        val credential = new OpenIdCredential(TGT_ID, USERNAME);
        val t = getTicketGrantingTicket();
        ticketRegistry.addTicket(t);

        assertEquals(TGT_ID, openIdCredentialsAuthenticationHandler.authenticate(credential, mock(Service.class)).getPrincipal().getId());
    }

    @Test
    public void verifyTGTThatIsExpired() throws Exception {
        val credential = new OpenIdCredential(TGT_ID, USERNAME);
        val t = getTicketGrantingTicket();
        ticketRegistry.addTicket(t);
        t.markTicketExpired();
        ticketRegistry.updateTicket(t);
        assertThrows(InvalidTicketException.class, () -> openIdCredentialsAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyTGTWithDifferentId() throws Exception {
        val credential = new OpenIdCredential(TGT_ID, "test1");
        val t = getTicketGrantingTicket();
        ticketRegistry.addTicket(t);
        assertThrows(FailedLoginException.class, () -> openIdCredentialsAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }

    private static TicketGrantingTicket getTicketGrantingTicket() {
        return new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), new HardTimeoutExpirationPolicy(10));
    }
}
