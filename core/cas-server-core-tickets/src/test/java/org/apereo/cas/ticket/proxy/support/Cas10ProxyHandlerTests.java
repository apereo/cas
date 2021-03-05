package org.apereo.cas.ticket.proxy.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Authentication")
public class Cas10ProxyHandlerTests {

    private final ProxyHandler proxyHandler = new Cas10ProxyHandler();

    @Test
    public void verifyNoCredentialsOrProxy() {
        assertNull(this.proxyHandler.handle(null, null));
    }

    @Test
    public void verifyCredentialsAndProxy() {
        val proxyGrantingTicket = mock(TicketGrantingTicket.class);
        when(proxyGrantingTicket.getId()).thenReturn("proxyGrantingTicket");
        assertNull(this.proxyHandler.handle(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), proxyGrantingTicket));
    }
}
