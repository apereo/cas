package org.jasig.cas.services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class SimpleServiceTests {

    @Test
    public void verifyProperId() {
        assertEquals("Ids are not equal.", TestUtils.CONST_TEST_URL, TestUtils.getService().getId());
    }

    @Test
    public void verifyEqualsWithNull() {
        assertNotEquals("Service matches null.", TestUtils.getService(), null);
    }

    @Test
    public void verifyEqualsWithBadClass() {
        assertFalse("Services matches String class.", TestUtils.getService().equals(new Object()));
    }

    @Test
    public void verifyEquals() {
        assertTrue("Services are not equal.", TestUtils.getService().equals(TestUtils.getService()));
    }
}
