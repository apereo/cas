package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class CasSimpleMultifactorAuthenticationTicketFactoryTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
    private TransientSessionTicketFactory ticketFactory;

    @Test
    public void verifyExpirationPolicy() {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"), new HashMap<>(0));
        assertNotNull(ticket);
        assertEquals(30, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    public void verifyCustomExpirationPolicy() {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"),
            CollectionUtils.wrap(ExpirationPolicy.class.getName(),
                HardTimeoutExpirationPolicy.builder().timeToKillInSeconds(60).build()));
        assertNotNull(ticket);
        assertEquals(60, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new CasSimpleMultifactorAuthenticationProvider();
    }
}
