package org.apereo.cas.services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class SimpleServiceTests {

    @Test
    public void verifyProperId() {
        assertEquals("Ids are not equal.", RegisteredServiceTestUtils.CONST_TEST_URL, RegisteredServiceTestUtils.getService().getId());
    }

    @Test
    public void verifyEqualsWithNull() {
        assertNotEquals("Service matches null.", RegisteredServiceTestUtils.getService(), null);
    }

    @Test
    public void verifyEqualsWithBadClass() {
        assertFalse("Services matches String class.", RegisteredServiceTestUtils.getService().equals(new Object()));
    }

    @Test
    public void verifyEquals() {
        assertTrue("Services are not equal.", RegisteredServiceTestUtils.getService().equals(RegisteredServiceTestUtils.getService()));
    }
}
