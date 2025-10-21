package org.apereo.cas.monitor;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.BaseJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.integration.autoconfigure.IntegrationProperties;
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
@SpringBootTest(classes = BaseJpaTicketRegistryTests.SharedTestConfiguration.class, properties = {
    "cas.jdbc.show-sql=false",
    "cas.ticket.registry.jpa.ddl-auto=create-drop",
    "cas.ticket.registry.jpa.user=root",
    "cas.ticket.registry.jpa.password=mypass",
    "cas.ticket.registry.jpa.driver-class=org.mariadb.jdbc.Driver",
    "cas.ticket.registry.jpa.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.MariaDBDialect"
})
@EnableConfigurationProperties({IntegrationProperties.class, CasConfigurationProperties.class})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MariaDb")
@ExtendWith(CasTestExtension.class)
class SessionHealthIndicatorJpaTests {
    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);

    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    private TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry jpaRegistry;

    private void addTicketsToRegistry(final TicketRegistry registry,
                                      final int tgtCount, final int stCount) throws Throwable {
        for (var i = 0; i < tgtCount; i++) {
            val ticket = new TicketGrantingTicketImpl(GENERATOR.getNewTicketId("TGT"),
                CoreAuthenticationTestUtils.getAuthentication(), TEST_EXP_POLICY);
            registry.addTicket(ticket);
            val testService = RegisteredServiceTestUtils.getService("junit");
            for (var j = 0; j < stCount; j++) {
                registry.addTicket(ticket.grantServiceTicket(GENERATOR.getNewTicketId("ST"),
                    testService, TEST_EXP_POLICY,
                    false, serviceTicketSessionTrackingPolicy));
            }
        }
    }

    @Test
    @Rollback(false)
    void verifyObserveOkJpaTicketRegistry() throws Throwable {
        addTicketsToRegistry(jpaRegistry, 5, 5);
        val monitor = new TicketRegistryHealthIndicator(new DirectObjectProvider<>(jpaRegistry), -1, -1);
        val status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }
}
