package org.apereo.cas.jmx.services;

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
 * This is {@link ServicesManagerManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmxTests.SharedTestConfiguration.class)
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class ServicesManagerManagedResourceTests {

    @Autowired
    @Qualifier("servicesManagerManagedResource")
    private ServicesManagerManagedResource servicesManagerManagedResource;

    @Test
    void verifyOperation() {
        assertNotNull(this.servicesManagerManagedResource);
        assertNotNull(this.servicesManagerManagedResource.getServices());
    }
}
