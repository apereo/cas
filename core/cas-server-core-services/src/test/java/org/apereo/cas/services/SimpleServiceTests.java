package org.apereo.cas.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
public class SimpleServiceTests {

    @Test
    public void verifyProperId() {
        assertEquals(RegisteredServiceTestUtils.CONST_TEST_URL, RegisteredServiceTestUtils.getService().getId(), "Ids are not equal.");
    }

    @Test
    public void verifyEqualsWithNull() {
        assertNotEquals(RegisteredServiceTestUtils.getService(), null, "Service matches null.");
    }

    @Test
    public void verifyEqualsWithBadClass() {
        assertNotEquals(RegisteredServiceTestUtils.getService(), new Object(), "Services matches String class.");
    }

    @Test
    public void verifyEquals() {
        assertEquals(RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getService(), "Services are not equal.");
    }
}
