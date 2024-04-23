package org.apereo.cas.integration.pac4j;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.TicketRegistrySessionStore;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.InvalidCookieException;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketRegistrySessionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseSessionStoreTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Web")
class TicketRegistrySessionStoreTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory ticketFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    private HttpServletRequest request;
    private MockHttpServletResponse response;

    private SessionStore sessionStore;

    private WebContext webContext;

    private CasCookieBuilder cookieGenerator;

    @BeforeEach
    public void setup() {
        ticketRegistry.deleteAll();

        val cookie = casProperties.getAuthn().getPac4j().getCore().getSessionReplication().getCookie();
        cookie.setName("DISSESSIONxxx");
        this.cookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookie);

        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();

        this.sessionStore = new TicketRegistrySessionStore(ticketRegistry, ticketFactory, cookieGenerator);
        this.webContext = new JEEContext(request, response);
    }

    @Test
    void verifyTracking() throws Throwable {
        assertNotNull(request.getSession());
        assertFalse(sessionStore.renewSession(webContext));
        assertTrue(sessionStore.buildFromTrackableSession(webContext, "trackable-session").isPresent());
        assertTrue(sessionStore.getTrackableSession(webContext).isPresent());
    }

    @Test
    void verifyCookieValue() throws Throwable {
        assertTrue(sessionStore.get(webContext, "SessionAttribute1").isEmpty());
        assertTrue(sessionStore.getSessionId(webContext, true).isEmpty());
        assertThrows(InvalidCookieException.class, this::getDistributedSessionCookie);
        sessionStore.set(webContext, "SessionAttribute1", "AttributeValue1");
        val cookie = getDistributedSessionCookie();
        val sessionCookieValue = cookieGenerator.getCasCookieValueManager().obtainCookieValue(cookie.getValue(), request);
        assertNotNull(sessionCookieValue);
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(sessionCookieValue);
        assertNotNull(ticketRegistry.getTicket(ticketId));
        assertFalse(sessionStore.get(webContext, "SessionAttribute1").isEmpty());
    }

    private Cookie getDistributedSessionCookie() {
        return Arrays.stream(response.getCookies())
            .filter(r -> r.getName().equals(cookieGenerator.getCookieName()))
            .findFirst()
            .orElseThrow(() -> new InvalidCookieException("Cookie not found"));
    }

    @Test
    void verifySetGet() throws Throwable {
        assertTrue(sessionStore.getSessionId(webContext, false).isEmpty());
        sessionStore.set(webContext, "attribute", "test");
        assertTrue(sessionStore.getSessionId(webContext, false).isPresent());
        var value = sessionStore.get(webContext, "attribute");
        assertTrue(value.isPresent());
        assertEquals("test", value.get());

        sessionStore.set(webContext, "attribute", "test2");
        value = sessionStore.get(webContext, "attribute");
        assertTrue(value.isPresent());
        assertEquals("test2", value.get());

        sessionStore.set(webContext, "attribute", null);
        sessionStore.set(webContext, "attribute2", "test3");
        assertFalse(sessionStore.get(webContext, "attribute").isPresent());
        value = sessionStore.get(webContext, "attribute2");
        assertTrue(value.isPresent());
        assertEquals("test3", value.get());

        assertDoesNotThrow(() -> sessionStore.set(webContext, "not-serializable", new NoSerializable()));
        sessionStore.destroySession(webContext);
        value = sessionStore.get(webContext, "attribute");
        assertTrue(value.isEmpty());

        assertTrue(sessionStore.getSessionId(webContext, false).isPresent());
    }

    private static final class NoSerializable {
    }
}
