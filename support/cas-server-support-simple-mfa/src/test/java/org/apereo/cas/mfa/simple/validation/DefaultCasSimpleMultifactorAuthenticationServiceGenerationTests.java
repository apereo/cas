package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketImpl;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationServiceGenerationTests}.
 *
 * @author Jerome LELEU
 * @since 7.3.0
 */
@Tag("MFAProvider")
class DefaultCasSimpleMultifactorAuthenticationServiceGenerationTests {

    private TicketRegistry ticketRegistry;

    private CasSimpleMultifactorAuthenticationTicketFactory ticketFactory;

    private DefaultCasSimpleMultifactorAuthenticationService service;

    @BeforeEach
    public void setUp() {
        ticketRegistry = mock(TicketRegistry.class);
        ticketFactory = new MockCasSimpleMultifactorAuthenticationTicketFactory(1);
        service = new DefaultCasSimpleMultifactorAuthenticationService(ticketRegistry, ticketFactory, null);
    }

    @Test
    void verifyGenerateWithNoTickets() throws Throwable {
        val token = service.generate(mock(Principal.class), mock(Service.class));
        assertEquals("CASMFA-1", token.getId());
    }

    @Test
    void verifyGenerateWithAFewTickets() throws Throwable {
        val mockTicket = mock(Ticket.class);
        when(ticketRegistry.getTicket("CASMFA-1")).thenReturn(mockTicket);
        when(ticketRegistry.getTicket("CASMFA-2")).thenReturn(mockTicket);
        val token = service.generate(mock(Principal.class), mock(Service.class));
        assertEquals("CASMFA-3", token.getId());
    }

    @Test
    void verifyGenerateWithTooManyTickets() throws Throwable {
        val mockTicket = mock(Ticket.class);
        when(ticketRegistry.getTicket("CASMFA-1")).thenReturn(mockTicket);
        when(ticketRegistry.getTicket("CASMFA-2")).thenReturn(mockTicket);
        when(ticketRegistry.getTicket("CASMFA-3")).thenReturn(mockTicket);
        when(ticketRegistry.getTicket("CASMFA-4")).thenReturn(mockTicket);
        when(ticketRegistry.getTicket("CASMFA-5")).thenReturn(mockTicket);
        try {
            service.generate(mock(Principal.class), mock(Service.class));
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Unable to create multifactor authentication token for principal: "));
        }
    }

    @AllArgsConstructor
    private static final class MockCasSimpleMultifactorAuthenticationTicketFactory implements CasSimpleMultifactorAuthenticationTicketFactory {

        private int sequence;

        @Override
        public CasSimpleMultifactorAuthenticationTicket create(final Service service, final Map<String, Serializable> properties) throws Throwable {
            return new CasSimpleMultifactorAuthenticationTicketImpl(CasSimpleMultifactorAuthenticationTicket.PREFIX + "-" + sequence++,
                    NeverExpiresExpirationPolicy.INSTANCE, service, properties);
        }

        @Override
        public CasSimpleMultifactorAuthenticationTicket create(final String id, final Service service, final Map<String, Serializable> properties) {
            throw new UnsupportedOperationException("This method should not be called");
        }

        @Override
        public Class<? extends Ticket> getTicketType() {
            throw new UnsupportedOperationException("This method should not be called");
        }

        @Override
        public ExpirationPolicyBuilder getExpirationPolicyBuilder() {
            throw new UnsupportedOperationException("This method should not be called");
        }
    }
}
