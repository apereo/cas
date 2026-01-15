package org.apereo.cas.ticket.proxy.support;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
class Cas20ProxyHandlerTests {

    private Cas20ProxyHandler handler;

    @Mock
    private TicketGrantingTicket proxyGrantingTicket;

    Cas20ProxyHandlerTests() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void initialize() {
        val factory = new SimpleHttpClientFactoryBean();
        factory.setConnectionTimeout(10000);
        this.handler = new Cas20ProxyHandler(factory.getObject(), new DefaultUniqueTicketIdGenerator());
        when(this.proxyGrantingTicket.getId()).thenReturn("proxyGrantingTicket");
    }

    @Test
    void verifyValidProxyTicketWithoutQueryString() throws Throwable {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredential(new URI("https://www.google.com/").toURL(),
            CoreAuthenticationTestUtils.getRegisteredService("https://some.app.edu")), proxyGrantingTicket));
    }

    @Test
    void verifyValidProxyTicketWithQueryString() throws Throwable {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredential(new URI("https://www.google.com/?test=test").toURL(),
            CoreAuthenticationTestUtils.getRegisteredService("https://some.app.edu")), proxyGrantingTicket));
    }

    @Test
    void verifyNonValidProxyTicket() throws Throwable {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(CollectionUtils.wrapList(900));

        this.handler = new Cas20ProxyHandler(clientFactory.getObject(), new DefaultUniqueTicketIdGenerator());

        assertNull(this.handler.handle(new HttpBasedServiceCredential(new URI("http://www.rutgers.edu").toURL(),
            CoreAuthenticationTestUtils.getRegisteredService("https://some.app.edu")), proxyGrantingTicket));
    }
}
