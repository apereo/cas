package org.apereo.cas.ticket.proxy.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas10ProxyHandlerTests {

    private final ProxyHandler proxyHandler = new Cas10ProxyHandler();

    @Test
    public void verifyNoCredentialsOrProxy() {
        assertNull(this.proxyHandler.handle(null, null));
    }

    @Test
    public void verifyCredentialsAndProxy() {
        final TicketGrantingTicket proxyGrantingTicket = mock(TicketGrantingTicket.class);
        when(proxyGrantingTicket.getId()).thenReturn("proxyGrantingTicket");
        assertNull(this.proxyHandler.handle(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), proxyGrantingTicket));
    }
}
