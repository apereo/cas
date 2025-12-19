package org.apereo.cas.jmx.ticket;

import module java.base;
import org.apereo.cas.jmx.BaseCasJmxTests;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketRegistryManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmxTests.SharedTestConfiguration.class)
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class TicketRegistryManagedResourceTests {
    @Autowired
    @Qualifier("ticketRegistryManagedResource")
    private TicketRegistryManagedResource ticketRegistryManagedResource;

    @Test
    void verifyOperation() {
        assertNotNull(this.ticketRegistryManagedResource);
        assertNotNull(this.ticketRegistryManagedResource.getTickets());
    }
}
