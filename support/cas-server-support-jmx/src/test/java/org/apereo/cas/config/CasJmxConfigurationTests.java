package org.apereo.cas.config;

import org.apereo.cas.jmx.BaseCasJmsTests;
import org.apereo.cas.jmx.services.ServicesManagerManagedResource;
import org.apereo.cas.jmx.ticket.TicketRegistryManagedResource;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJmxConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmsTests.SharedTestConfiguration.class)
@Tag("JMX")
public class CasJmxConfigurationTests {

    @Autowired
    @Qualifier("servicesManagerManagedResource")
    private ServicesManagerManagedResource servicesManagerManagedResource;

    @Autowired
    @Qualifier("ticketRegistryManagedResource")
    private TicketRegistryManagedResource ticketRegistryManagedResource;

    @Test
    public void verifyOperation() {
        assertNotNull(this.servicesManagerManagedResource);
        assertNotNull(this.ticketRegistryManagedResource);
    }

}
