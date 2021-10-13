package org.apereo.cas.integration.pac4j;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.web.support.CookieUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DistributedJEESessionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseSessionStoreTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Web")
public class DistributedJEESessionStoreTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Test
    public void verifyTracking() {
        val cookie = casProperties.getSessionReplication().getCookie();
        val cookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookie);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val store = new DistributedJEESessionStore(centralAuthenticationService, ticketFactory, cookieGenerator);
        val context = new JEEContext(request, response);

        assertNotNull(request.getSession());

        assertFalse(store.renewSession(context));
        assertTrue(store.buildFromTrackableSession(context, "trackable-session").isPresent());
        assertTrue(store.getTrackableSession(context).isPresent());
    }

    @Test
    public void verifySetGet() {
        val cookie = casProperties.getSessionReplication().getCookie();
        val cookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookie);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val store = new DistributedJEESessionStore(centralAuthenticationService, ticketFactory, cookieGenerator);
        val context = new JEEContext(request, response);

        assertTrue(store.getSessionId(context, false).isEmpty());
        store.set(context, "attribute", "test");
        assertTrue(store.getSessionId(context, false).isPresent());
        var value = store.get(context, "attribute");
        assertTrue(value.isPresent());
        assertEquals("test", value.get());

        store.set(context, "attribute", "test2");
        value = store.get(context, "attribute");
        assertTrue(value.isPresent());
        assertEquals("test2", value.get());

        store.set(context, "attribute", null);
        store.set(context, "attribute2", "test3");
        assertFalse(store.get(context, "attribute").isPresent());
        value = store.get(context, "attribute2");
        assertTrue(value.isPresent());
        assertEquals("test3", value.get());

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                store.set(context, "not-serializable", new NoSerializable());
            }
        });
        store.destroySession(context);
        value = store.get(context, "attribute");
        assertTrue(value.isEmpty());

        assertTrue(store.getSessionId(context, false).isPresent());
    }

    private static class NoSerializable {
    }
}
