package org.apereo.cas.jmx.ticket;

import org.apereo.cas.jmx.BaseCasJmsTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@SpringBootTest(classes = BaseCasJmsTests.SharedTestConfiguration.class)
@Tag("JMX")
public class TicketRegistryManagedResourceTests {
    @Autowired
    @Qualifier("ticketRegistryManagedResource")
    private TicketRegistryManagedResource ticketRegistryManagedResource;

    @Test
    public void verifyOperation() {
        assertNotNull(this.ticketRegistryManagedResource);
        assertNotNull(this.ticketRegistryManagedResource.getTickets());
    }
}
