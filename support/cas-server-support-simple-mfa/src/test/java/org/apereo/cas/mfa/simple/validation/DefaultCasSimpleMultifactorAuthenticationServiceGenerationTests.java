package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketImpl;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationServiceGenerationTests}.
 *
 * @author Jerome LELEU
 * @since 7.3.0
 */
@SpringBootTest(classes = {
    DefaultCasSimpleMultifactorAuthenticationServiceTests.DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration.class,
    BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class DefaultCasSimpleMultifactorAuthenticationServiceGenerationTests {

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    private CasSimpleMultifactorAuthenticationTicketFactory ticketFactory;

    private CasSimpleMultifactorAuthenticationService customService;

    @BeforeEach
    public void setUp() {
        ticketRegistry.deleteAll();
        ticketFactory = new MockCasSimpleMultifactorAuthenticationTicketFactory(1);
        customService = new DefaultCasSimpleMultifactorAuthenticationService(ticketRegistry, ticketFactory, null);
    }

    @Test
    void verifyGenerateWithNoTickets() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val service = CoreAuthenticationTestUtils.getService("mfa-simple");
        val token = customService.generate(principal, service);
        assertEquals("CASMFA-1", token.getId());
    }

    @Test
    void verifyGenerateWithAFewTickets() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val service = CoreAuthenticationTestUtils.getService("mfa-simple");
        addTicket("CASMFA-1", service);
        addTicket("CASMFA-2", service);
        val token = customService.generate(principal, service);
        assertEquals("CASMFA-3", token.getId());
    }

    @Test
    void verifyGenerateWithTooManyTickets() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val service = CoreAuthenticationTestUtils.getService("mfa-simple");
        addTicket("CASMFA-1", service);
        addTicket("CASMFA-2", service);
        addTicket("CASMFA-3", service);
        addTicket("CASMFA-4", service);
        addTicket("CASMFA-5", service);
        assertThrows(IllegalArgumentException.class, () -> customService.generate(principal, service));
    }

    private void addTicket(final String ticketId, final Service service) throws Exception {
        val ticket = new CasSimpleMultifactorAuthenticationTicketImpl(ticketId, NeverExpiresExpirationPolicy.INSTANCE, service, new HashMap<>());
        ticketRegistry.addTicket(ticket);
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
