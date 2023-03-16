package org.apereo.cas.ticket.monitoring;

import org.apereo.cas.config.CasCoreTicketsMonitoringConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.monitor.config.CasCoreMonitorConfiguration;
import org.apereo.cas.ticket.registry.BaseTicketRegistryTests;
import org.apereo.cas.ticket.registry.TicketRegistry;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationTextPublisher;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreTicketsMonitoringConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasCoreTicketsMonitoringConfigurationTests.CasCoreTicketsMonitoringTestConfiguration.class,
    CasCoreMonitorConfiguration.class,
    CasCoreTicketsMonitoringConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
})
@Tag("Tickets")
@EnableAspectJAutoProxy(proxyTargetClass = false)
@AutoConfigureObservability
public class CasCoreTicketsMonitoringConfigurationTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;
    
    private static final List<String> ENTRIES = new ArrayList<>();

    @Test
    public void verifyOperation() throws Exception {
        ticketRegistry.addTicket(new MockTicketGrantingTicket("casuser"));
        val tickets = ticketRegistry.getTickets();
        assertFalse(tickets.isEmpty());
        assertFalse(ENTRIES.isEmpty());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CasCoreTicketsMonitoringTestConfiguration {
        @Bean
        public ObservationHandler<Observation.Context> collectingObservationHandler() {
            return new ObservationTextPublisher(ENTRIES::add);
        }
    }
}
