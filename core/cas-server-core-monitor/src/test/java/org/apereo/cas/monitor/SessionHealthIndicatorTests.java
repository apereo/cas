package org.apereo.cas.monitor;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link TicketRegistryHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Tag("Metrics")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = CasCoreMonitorAutoConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SessionHealthIndicatorTests {

    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);

    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    protected TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    private TicketRegistry defaultRegistry;

    private void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        val ticket = new TicketGrantingTicketImpl[]{null};
        IntStream.range(0, tgtCount).forEach(Unchecked.intConsumer(i -> {
            ticket[0] = new TicketGrantingTicketImpl(GENERATOR.getNewTicketId("TGT"), CoreAuthenticationTestUtils.getAuthentication(), TEST_EXP_POLICY);
            registry.addTicket(ticket[0]);
        }));

        if (ticket[0] != null) {
            val testService = getService("junit");
            IntStream.range(0, stCount).forEach(Unchecked.intConsumer(i -> registry.addTicket(ticket[0].grantServiceTicket(GENERATOR.getNewTicketId("ST"),
                testService, TEST_EXP_POLICY, false, serviceTicketSessionTrackingPolicy))));
        }
    }

    public static AbstractWebApplicationService getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    @BeforeEach
    public void initialize() {
        this.defaultRegistry = new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                mock(ConfigurableApplicationContext.class));
    }

    @Test
    void verifyObserveOk() throws Throwable {
        addTicketsToRegistry(this.defaultRegistry, 5, 10);
        val monitor = new TicketRegistryHealthIndicator(new DirectObjectProvider<>(defaultRegistry), -1, -1);
        val status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }

    @Test
    void verifyObserveWarnSessionsExceeded() throws Throwable {
        addTicketsToRegistry(this.defaultRegistry, 10, 1);
        val monitor = new TicketRegistryHealthIndicator(new DirectObjectProvider<>(defaultRegistry), 0, 5);
        val status = monitor.health();
        assertEquals("WARN", status.getStatus().getCode());
    }

    @Test
    void verifyObserveWarnServiceTicketsExceeded() throws Throwable {
        addTicketsToRegistry(this.defaultRegistry, 1, 10);
        val monitor = new TicketRegistryHealthIndicator(new DirectObjectProvider<>(defaultRegistry), 5, 0);
        val status = monitor.health();
        assertEquals("WARN", status.getStatus().getCode());
    }
}
