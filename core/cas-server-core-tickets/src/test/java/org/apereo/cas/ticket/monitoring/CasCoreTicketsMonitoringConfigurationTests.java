package org.apereo.cas.ticket.monitoring;

import org.apereo.cas.config.CasCoreMonitorAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.BaseTicketRegistryTests;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationTextPublisher;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreTicketsMonitoringConfigurationTests.CasCoreTicketsMonitoringTestConfiguration.class,
    CasCoreMonitorAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
})
@Tag("Tickets")
@ExtendWith(CasTestExtension.class)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@AutoConfigureObservability
class CasCoreTicketsMonitoringConfigurationTests {
    private static final List<String> ENTRIES = new ArrayList<>();
    
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;


    @Test
    void verifyOperation() throws Throwable {
        ticketRegistry.addTicket(new MockTicketGrantingTicket("casuser"));
        val tickets = ticketRegistry.getTickets();
        assertFalse(tickets.isEmpty());
        assertFalse(ENTRIES.isEmpty());
    }

    @TestConfiguration(value = "CasCoreTicketsMonitoringTestConfiguration", proxyBeanMethods = false)
    static class CasCoreTicketsMonitoringTestConfiguration {
        @Bean
        public ObservationHandler<Observation.Context> collectingObservationHandler() {
            return new ObservationTextPublisher(ENTRIES::add);
        }
    }
}
