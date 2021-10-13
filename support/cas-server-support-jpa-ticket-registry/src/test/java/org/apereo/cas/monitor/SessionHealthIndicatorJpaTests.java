package org.apereo.cas.monitor;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.JpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link TicketRegistryHealthIndicator} class that involves
 * {@link JpaTicketRegistry}.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@SpringBootTest(classes = JpaTicketRegistryTests.SharedTestConfiguration.class)
@Tag("JDBC")
public class SessionHealthIndicatorJpaTests {
    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);

    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry jpaRegistry;

    private static void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        for (var i = 0; i < tgtCount; i++) {
            val ticket = new TicketGrantingTicketImpl(GENERATOR.getNewTicketId("TGT"),
                CoreAuthenticationTestUtils.getAuthentication(), TEST_EXP_POLICY);
            registry.addTicket(ticket);
            val testService = RegisteredServiceTestUtils.getService("junit");
            for (var j = 0; j < stCount; j++) {
                registry.addTicket(ticket.grantServiceTicket(GENERATOR.getNewTicketId("ST"),
                    testService,
                    TEST_EXP_POLICY,
                    false, true));
            }
        }
    }

    @Test
    @Rollback(false)
    public void verifyObserveOkJpaTicketRegistry() {
        addTicketsToRegistry(jpaRegistry, 5, 5);
        val monitor = new TicketRegistryHealthIndicator(jpaRegistry, -1, -1);
        val status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }
}
