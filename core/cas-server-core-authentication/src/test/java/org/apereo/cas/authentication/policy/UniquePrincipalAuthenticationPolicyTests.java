package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UniquePrincipalAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    MailSenderAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreServicesConfiguration.class
})
@DirtiesContext
@Tag("Simple")
public class UniquePrincipalAuthenticationPolicyTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @SneakyThrows
    public void verifyPolicyIsGoodUserNotFound() {
        this.ticketRegistry.deleteAll();
        val p = new UniquePrincipalAuthenticationPolicy(this.ticketRegistry);
        assertTrue(p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            new LinkedHashSet<>(), applicationContext));
    }

    @Test
    @SneakyThrows
    public void verifyPolicyFailsUserFoundOnce() {
        this.ticketRegistry.deleteAll();
        val ticket = new TicketGrantingTicketImpl("TGT-1",
            CoreAuthenticationTestUtils.getAuthentication("casuser"),
            NeverExpiresExpirationPolicy.INSTANCE);
        this.ticketRegistry.addTicket(ticket);
        val p = new UniquePrincipalAuthenticationPolicy(this.ticketRegistry);
        assertThrows(UniquePrincipalRequiredException.class,
            () -> p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"),
                new LinkedHashSet<>(), applicationContext));
    }
}
