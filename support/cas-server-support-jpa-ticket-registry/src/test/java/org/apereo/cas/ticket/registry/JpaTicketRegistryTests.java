package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    JpaTicketRegistryTicketCatalogConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class,
    CasWsSecurityTokenTicketCatalogConfiguration.class
})
@Transactional(transactionManager = "ticketTransactionManager", isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
@ResourceLock("jpa-tickets")
@Tag("JDBC")
public class JpaTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @AfterEach
    public void cleanup() {
        ticketRegistry.deleteAll();
    }

    @Override
    protected TicketRegistry getNewTicketRegistry() {
        return this.ticketRegistry;
    }

    @RepeatedTest(2)
    public void verifySecurityTokenTicket() {
        val securityTokenTicketFactory = new DefaultSecurityTokenTicketFactory(
            new DefaultUniqueTicketIdGenerator(),
            neverExpiresExpirationPolicyBuilder());

        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgt = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        this.ticketRegistry.addTicket(tgt);

        val token = securityTokenTicketFactory.create(tgt, "dummy-token".getBytes(StandardCharsets.UTF_8));
        this.ticketRegistry.addTicket(token);

        assertNotNull(this.ticketRegistry.getTicket(token.getId()));
        this.ticketRegistry.deleteTicket(token);
        assertNull(this.ticketRegistry.getTicket(token.getId()));
    }
}
