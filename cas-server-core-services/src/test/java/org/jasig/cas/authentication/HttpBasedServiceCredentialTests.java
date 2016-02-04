package org.jasig.cas.authentication;

import org.jasig.cas.util.ServicesTestUtils;
import org.jasig.cas.util.AuthTestUtils;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class HttpBasedServiceCredentialTests {

    @Test
    public void verifyProperUrl() {
        assertEquals(AuthTestUtils.CONST_GOOD_URL, AuthTestUtils.getHttpBasedServiceCredentials().getCallbackUrl()
                .toExternalForm());
    }

    @Test
    public void verifyEqualsWithNull() throws Exception {
        final HttpBasedServiceCredential c = new HttpBasedServiceCredential(new URL("http://www.cnn.com"),
                ServicesTestUtils.getRegisteredService("https://some.app.edu"));

        assertNotEquals(c, null);
    }

    @Test
    public void verifyEqualsWithFalse() throws Exception {
        final HttpBasedServiceCredential c = new HttpBasedServiceCredential(new URL("http://www.cnn.com"),
                ServicesTestUtils.getRegisteredService("https://some.app.edu"));
        final HttpBasedServiceCredential c2 = new HttpBasedServiceCredential(new URL("http://www.msn.com"),
                ServicesTestUtils.getRegisteredService("https://some.app.edu"));

        assertFalse(c.equals(c2));
        assertFalse(c.equals(new Object()));
    }

    @Test
    public void verifyEqualsWithTrue() throws Exception {
        final HttpBasedServiceCredential c = new HttpBasedServiceCredential(new URL("http://www.cnn.com"),
                ServicesTestUtils.getRegisteredService("https://some.app.edu"));
        final HttpBasedServiceCredential c2 = new HttpBasedServiceCredential(new URL("http://www.cnn.com"),
                ServicesTestUtils.getRegisteredService("https://some.app.edu"));

        assertTrue(c.equals(c2));
        assertTrue(c2.equals(c));
    }
}
