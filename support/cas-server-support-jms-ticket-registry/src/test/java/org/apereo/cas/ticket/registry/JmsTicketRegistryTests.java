package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.JmsTicketRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link JmsTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Import({
    ActiveMQAutoConfiguration.class,
    JmsAutoConfiguration.class,
    JmsTicketRegistryConfiguration.class
})
@TestPropertySource(
    properties = {
        "spring.activemq.pool.enabled=false",
        "spring.activemq.packages.trust-all=true"
    })
@EnableJms
@EnabledIfListeningOnPort(port = 61616)
@Tag("JMS")
@Getter
public class JmsTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
