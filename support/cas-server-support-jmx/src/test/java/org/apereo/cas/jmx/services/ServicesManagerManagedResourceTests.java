package org.apereo.cas.jmx.services;

import org.apereo.cas.jmx.BaseCasJmsTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServicesManagerManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmsTests.SharedTestConfiguration.class)
@Tag("JMX")
public class ServicesManagerManagedResourceTests {

    @Autowired
    @Qualifier("servicesManagerManagedResource")
    private ServicesManagerManagedResource servicesManagerManagedResource;

    @Test
    public void verifyOperation() {
        assertNotNull(this.servicesManagerManagedResource);
        assertNotNull(this.servicesManagerManagedResource.getServices());
    }
}
