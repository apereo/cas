package org.apereo.cas.ticket.proxy.support;

import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class Cas20ProxyHandlerTests {

    private Cas20ProxyHandler handler;

    @Mock
    private TicketGrantingTicket proxyGrantingTicket;

    public Cas20ProxyHandlerTests() {
        MockitoAnnotations.initMocks(this);
    }
    @Before
    public void setUp() throws Exception {
        this.handler = new Cas20ProxyHandler();

        final SimpleHttpClientFactoryBean factory = new SimpleHttpClientFactoryBean();
        factory.setConnectionTimeout(10000);
        factory.setReadTimeout(10000);
        this.handler.setHttpClient(factory.getObject());
        this.handler.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        when(this.proxyGrantingTicket.getId()).thenReturn("proxyGrantingTicket");
    }

    @Test
    public void verifyValidProxyTicketWithoutQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredential(
            new URL("https://www.google.com/"),
                TestUtils.getRegisteredService("https://some.app.edu")), proxyGrantingTicket));
    }

    @Test
    public void verifyValidProxyTicketWithQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredential(
            new URL("https://www.google.com/?test=test"),
                        TestUtils.getRegisteredService("https://some.app.edu")),
                proxyGrantingTicket));
    }

    @Test
    public void verifyNonValidProxyTicket() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(new int[] {900});
        final HttpClient httpClient = clientFactory.getObject();
        this.handler.setHttpClient(httpClient);
        assertNull(this.handler.handle(new HttpBasedServiceCredential(new URL(
            "http://www.rutgers.edu"),
                TestUtils.getRegisteredService("https://some.app.edu")), proxyGrantingTicket));
    }
}
