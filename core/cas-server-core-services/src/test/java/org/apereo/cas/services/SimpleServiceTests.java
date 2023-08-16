package org.apereo.cas.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Authentication")
class SimpleServiceTests {

    @Test
    void verifyProperId() throws Throwable {
        assertEquals(RegisteredServiceTestUtils.CONST_TEST_URL, RegisteredServiceTestUtils.getService().getId(), "Ids are not equal.");
    }

    @Test
    void verifyEqualsWithNull() throws Throwable {
        assertNotEquals(null, RegisteredServiceTestUtils.getService(), "Service matches null.");
    }

    @Test
    void verifyEqualsWithBadClass() throws Throwable {
        assertNotEquals(new Object(), RegisteredServiceTestUtils.getService(), "Services matches String class.");
    }

    @Test
    void verifyEquals() throws Throwable {
        assertEquals(RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getService(), "Services are not equal.");
    }
}
