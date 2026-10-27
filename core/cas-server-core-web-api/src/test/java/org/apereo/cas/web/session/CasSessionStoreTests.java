package org.apereo.cas.web.session;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import jakarta.servlet.http.HttpSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasSessionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Web")
class CasSessionStoreTests {

    @Nested
    class DefaultStoreTests {
        @Test
        void verifyValuesAreKeptUnderPrefix() {
            val store = new DefaultCasSessionStore("Prefix");
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            assertTrue(store.get(request, "key").isEmpty());
            assertTrue(store.getTrackableSession(request).isEmpty());

            store.set(request, response, "key", "value");
            assertEquals("value", store.get(request, "key").orElseThrow());
            assertEquals("value", Objects.requireNonNull(request.getSession(false)).getAttribute("Prefixkey"));
            assertInstanceOf(HttpSession.class, store.getTrackableSession(request).orElseThrow());

            store.set(request, response, "key", null);
            assertTrue(store.get(request, "key").isEmpty());
            assertTrue(store.buildFromTrackableSession(request, "ignored").isEmpty());
        }

        @Test
        void verifySessionLifecycle() {
            val store = new DefaultCasSessionStore("Prefix");
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            assertTrue(store.getSessionId(request, false).isEmpty());
            store.set(request, response, "key", "value");
            val sessionId = store.getSessionId(request, false).orElseThrow();

            assertTrue(store.renewSession(request));
            assertNotEquals(sessionId, store.getSessionId(request, false).orElseThrow());
            assertEquals("value", store.get(request, "key").orElseThrow());

            val tracked = (MockHttpSession) store.getTrackableSession(request).orElseThrow();
            val otherRequest = new MockHttpServletRequest();
            val rebuilt = store.buildFromTrackableSession(otherRequest, tracked).orElseThrow();
            assertEquals("value", rebuilt.get(otherRequest, "key").orElseThrow());
            assertEquals(tracked.getId(), rebuilt.getSessionId(otherRequest, false).orElseThrow());
            assertTrue(rebuilt.destroySession(otherRequest, new MockHttpServletResponse()));
            assertTrue(tracked.isInvalid());
            assertNull(otherRequest.getSession(false));
        }

        @Test
        void verifyRemovalWithoutSession() {
            val store = new DefaultCasSessionStore();
            val request = new MockHttpServletRequest();
            store.set(request, new MockHttpServletResponse(), "key", null);
            assertNull(request.getSession(false));
        }
    }

    @Nested
    class BrowserStorageStoreTests {
        @Test
        void verifySessionTravelsThroughBrowserStorage() {
            val cipher = new WebflowConversationStateCipherExecutor("AES", 512, 16);
            val store = new BrowserStorageCasSessionStore(cipher, "ContextKey");
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            store.set(request, response, "key1", "value1");
            store.withSessionAttributes(request, Map.of("key2", new ArrayList<>(List.of("HelloWorld")), "key3", 1234567));

            val storage = (BrowserStorage) store.getTrackableSession(request).orElseThrow();
            assertEquals("ContextKey", storage.getContext());
            assertEquals(BrowserStorage.BrowserStorageTypes.LOCAL, storage.getStorageType());

            val restoredRequest = new MockHttpServletRequest();
            assertSame(store, store.buildFromTrackableSession(restoredRequest, toBrowserPayload(storage)).orElseThrow());
            assertEquals("value1", store.get(restoredRequest, "key1").orElseThrow());
            assertEquals("value1", store.getSessionAttributes(restoredRequest).get("key1"));
            assertTrue(store.getSessionAttributes(new MockHttpServletRequest()).isEmpty());
            assertEquals(List.of("HelloWorld"), store.get(restoredRequest, "key2").orElseThrow());
            assertEquals(1234567, store.get(restoredRequest, "key3").orElseThrow());
        }

        @Test
        void verifyEmptySession() {
            val cipher = new WebflowConversationStateCipherExecutor("AES", 512, 16);
            val store = new BrowserStorageCasSessionStore(cipher, "ContextKey");
            val storage = (BrowserStorage) store.getTrackableSession(new MockHttpServletRequest()).orElseThrow();
            val restoredRequest = new MockHttpServletRequest();
            store.buildFromTrackableSession(restoredRequest, toBrowserPayload(storage));
            assertTrue(store.get(restoredRequest, "key1").isEmpty());
        }
    }

    /**
     * Build what the browser posts back after reading a browser storage entry: a JSON object that maps
     * the storage context to the stored payload.
     *
     * @param storage the browser storage
     * @return the payload
     */
    private static String toBrowserPayload(final BrowserStorage storage) {
        return "{\"%s\":\"%s\"}".formatted(storage.getContext(), storage.getPayload());
    }

    @Nested
    @SuppressWarnings("removal")
    class TicketRegistryStoreTests {
        private final Map<String, TransientSessionTicket> tickets = new HashMap<>();

        private TicketRegistry ticketRegistry;

        private TicketFactory ticketFactory;

        private CasCookieBuilder cookieGenerator;

        private MockHttpServletRequest request;

        private MockHttpServletResponse response;

        @BeforeEach
        void setup() throws Throwable {
            tickets.clear();
            request = new MockHttpServletRequest();
            response = new MockHttpServletResponse();
            cookieGenerator = mock(CasCookieBuilder.class);
            when(cookieGenerator.getCookieName()).thenReturn("SessionCookie");

            val transientFactory = mock(TransientSessionTicketFactory.class);
            when(transientFactory.create(anyString(), anyMap())).thenAnswer(invocation -> {
                val id = TransientSessionTicketFactory.normalizeTicketId(invocation.getArgument(0));
                return newTicket(id, invocation.getArgument(1));
            });
            ticketFactory = mock(TicketFactory.class);
            when(ticketFactory.get(TransientSessionTicket.class)).thenReturn(transientFactory);

            ticketRegistry = mock(TicketRegistry.class);
            when(ticketRegistry.addTicket(any(Ticket.class))).thenAnswer(invocation -> {
                val ticket = (TransientSessionTicket) invocation.getArgument(0);
                tickets.put(ticket.getId(), ticket);
                return ticket;
            });
            when(ticketRegistry.updateTicket(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(ticketRegistry.getTicket(anyString(), eq(TransientSessionTicket.class)))
                .thenAnswer(invocation -> tickets.get(invocation.<String>getArgument(0)));
        }

        @Test
        void verifyValuesAreKeptInTicket() {
            val store = new TicketRegistryCasSessionStore(ticketRegistry, ticketFactory, cookieGenerator);
            assertTrue(store.get(request, "key").isEmpty());
            assertTrue(store.getTrackableSession(request).isEmpty());

            store.set(request, response, "key", "value");
            val ticketId = (String) store.getTrackableSession(request).orElseThrow();
            verify(cookieGenerator).addCookie(request, response, ticketId);
            assertEquals("value", store.get(request, "key").orElseThrow());

            val values = new ArrayList<>(List.of("first", "second"));
            store.set(request, response, "values", values);
            store.set(request, response, "text", "rO0AB-looking text");
            assertEquals(values, store.get(request, "values").orElseThrow());
            assertEquals("rO0AB-looking text", store.get(request, "text").orElseThrow());
            assertInstanceOf(String.class, tickets.get(ticketId).getProperties().get("values"));

            store.set(request, response, "key", null);
            assertTrue(store.get(request, "key").isEmpty());

            val restoredRequest = new MockHttpServletRequest();
            store.buildFromTrackableSession(restoredRequest, ticketId);
            assertEquals(values, store.get(restoredRequest, "values").orElseThrow());
        }

        @Test
        void verifyCookieFollowsUpdatedTicket() throws Throwable {
            val storedTicket = newTicket("TST-stored", new HashMap<>());
            tickets.put(storedTicket.getId(), storedTicket);
            val updatedTicket = mock(Ticket.class);
            when(updatedTicket.getId()).thenReturn("TST-updated");
            when(ticketRegistry.updateTicket(any(Ticket.class))).thenReturn(updatedTicket);

            val store = new TicketRegistryCasSessionStore(ticketRegistry, ticketFactory, cookieGenerator);
            store.buildFromTrackableSession(request, storedTicket.getId());
            store.set(request, response, "key", "value");
            assertEquals("TST-updated", store.getTrackableSession(request).orElseThrow());
            verify(cookieGenerator).addCookie(request, response, "TST-updated");
        }

        @Test
        void verifySessionDestroyed() throws Throwable {
            val store = new TicketRegistryCasSessionStore(ticketRegistry, ticketFactory, cookieGenerator);
            assertTrue(store.getSessionId(request, true).isEmpty());
            assertFalse(store.renewSession(request));
            store.set(request, response, "key", "value");
            val ticketId = store.getSessionId(request, false).orElseThrow();
            assertTrue(store.destroySession(request, response));
            verify(ticketRegistry).deleteTicket(ticketId);
            verify(cookieGenerator).removeCookie(response);
        }

        @Test
        void verifySessionIdFromCookie() {
            val storedTicket = newTicket("TST-cookie", new HashMap<>(Map.of("key", "value")));
            val ticketId = storedTicket.getId();
            tickets.put(ticketId, storedTicket);
            when(cookieGenerator.retrieveCookieValue(request)).thenReturn(ticketId);
            val store = new TicketRegistryCasSessionStore(ticketRegistry, ticketFactory, cookieGenerator);
            assertEquals("value", store.get(request, "key").orElseThrow());
        }

        private static TransientSessionTicket newTicket(final String id, final Map<String, ?> properties) {
            val ticket = mock(TransientSessionTicket.class);
            when(ticket.getId()).thenReturn(id);
            when(ticket.getProperties()).thenReturn(new HashMap<String, Object>(properties));
            return ticket;
        }
    }
}
