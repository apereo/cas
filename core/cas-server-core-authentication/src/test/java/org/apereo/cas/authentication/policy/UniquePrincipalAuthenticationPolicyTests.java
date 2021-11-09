package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
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
import org.apereo.cas.validation.Assertion;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.LinkedHashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UniquePrincipalAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreServicesConfiguration.class
})
@Tag("AuthenticationPolicy")
public class UniquePrincipalAuthenticationPolicyTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyPolicyIsGoodUserNotFound() {
        this.ticketRegistry.deleteAll();
        val p = new UniquePrincipalAuthenticationPolicy(this.ticketRegistry);
        assertTrue(p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            new LinkedHashSet<>(), applicationContext, Optional.empty()).isSuccess());
    }

    @Test
    public void verifyPolicyWithAssertion() throws Exception {
        this.ticketRegistry.deleteAll();
        val p = new UniquePrincipalAuthenticationPolicy(this.ticketRegistry);
        assertTrue(p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            new LinkedHashSet<>(), applicationContext, Optional.of(mock(Assertion.class))).isSuccess());
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
                new LinkedHashSet<>(), applicationContext, Optional.empty()));
    }
}
