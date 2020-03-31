package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.JmsTicketRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.EnableJms;

/**
 * This is {@link JmsTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    ActiveMQAutoConfiguration.class,
    JmsAutoConfiguration.class,
    JmsTicketRegistryConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {"spring.activemq.pool.enabled=false", "spring.activemq.packages.trust-all=true"})
@EnableJms
@EnabledIfPortOpen(port = 61616)
@Tag("JMS")
public class JmsTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }
}
