package org.apereo.cas.mfa.simple;

import module java.base;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketCompactor;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class CasSimpleMultifactorAuthenticationTicketFactoryTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
    private CasSimpleMultifactorAuthenticationTicketFactory ticketFactory;

    @Test
    void verifyExpirationPolicy() throws Throwable {
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) this.ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"), new HashMap<>());
        assertNotNull(ticket);
        assertEquals(30, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) this.ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"),
            CollectionUtils.wrap(ExpirationPolicy.class.getName(),
                HardTimeoutExpirationPolicy.builder().timeToKillInSeconds(60).build()));
        assertNotNull(ticket);
        assertEquals(60, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyCompactedTokenKeepsCodeServiceAndPrincipal() throws Throwable {
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) this.ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val service = RegisteredServiceTestUtils.getService("https://example.org/app?param=value");
        val ticket = factory.create(service, CollectionUtils.<String, Serializable>wrap(
            CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, RegisteredServiceTestUtils.getPrincipal("casuser")));
        val serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.createService(service.getId())).thenReturn(service);
        val compactor = new CasSimpleMultifactorAuthenticationTicketCompactor(new DirectObjectProvider<TicketFactory>(ticketFactory),
            serviceFactory, PrincipalFactoryUtils.newPrincipalFactory());

        val expanded = (CasSimpleMultifactorAuthenticationTicket) compactor.expand(compactor.compact(ticket));
        assertEquals(ticket.getId(), CasSimpleMultifactorAuthenticationTicket.getCode(expanded));
        assertEquals(service.getId(), Objects.requireNonNull(expanded.getService()).getId());
        assertEquals("casuser", ((Principal) expanded.getProperties().get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL)).getId());
        assertEquals(ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond(),
            expanded.getExpirationPolicy().toMaximumExpirationTime(expanded).toEpochSecond());

        val expandedAgain = (CasSimpleMultifactorAuthenticationTicket) compactor.expand(compactor.compact(expanded));
        assertEquals(ticket.getId(), CasSimpleMultifactorAuthenticationTicket.getCode(expandedAgain));
    }

    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new CasSimpleMultifactorAuthenticationProvider();
    }
}
