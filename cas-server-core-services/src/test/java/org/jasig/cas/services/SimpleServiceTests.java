package org.jasig.cas.services;

import org.jasig.cas.util.ServicesTestUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class SimpleServiceTests {

    @Test
    public void verifyProperId() {
        assertEquals("Ids are not equal.", ServicesTestUtils.CONST_TEST_URL, ServicesTestUtils.getService().getId());
    }

    @Test
    public void verifyEqualsWithNull() {
        assertNotEquals("Service matches null.", ServicesTestUtils.getService(), null);
    }

    @Test
    public void verifyEqualsWithBadClass() {
        assertFalse("Services matches String class.", ServicesTestUtils.getService().equals(new Object()));
    }

    @Test
    public void verifyEquals() {
        assertTrue("Services are not equal.", ServicesTestUtils.getService().equals(ServicesTestUtils.getService()));
    }
}
