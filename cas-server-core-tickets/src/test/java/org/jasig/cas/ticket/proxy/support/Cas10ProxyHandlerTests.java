package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;
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
                org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword(), proxyGrantingTicket));
    }
}
