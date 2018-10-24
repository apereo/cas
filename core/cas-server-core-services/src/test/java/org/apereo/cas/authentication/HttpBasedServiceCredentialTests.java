package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class HttpBasedServiceCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "httpBasedServiceCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final String CNN_URL = "http://www.cnn.com";
    private static final String SOME_APP_URL = "https://some.app.edu";

    @Test
    public void verifyProperUrl() {
        assertEquals(CoreAuthenticationTestUtils.CONST_GOOD_URL,
            CoreAuthenticationTestUtils.getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }

    @Test
    public void verifyEqualsWithNull() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(SOME_APP_URL);
        val c = new HttpBasedServiceCredential(new URL(CNN_URL), registeredService);
        assertNotEquals(c, null);
    }

    @Test
    public void verifyEqualsWithFalse() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(SOME_APP_URL);
        val c = new HttpBasedServiceCredential(new URL(CNN_URL), registeredService);
        val c2 = new HttpBasedServiceCredential(new URL("http://www.msn.com"), registeredService);
        assertFalse(c.equals(c2));
        assertFalse(c.equals(new Object()));
    }

    @Test
    public void verifyEqualsWithTrue() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(SOME_APP_URL);
        val callbackUrl = new URL(CNN_URL);
        val c = new HttpBasedServiceCredential(callbackUrl, registeredService);
        val c2 = new HttpBasedServiceCredential(callbackUrl, registeredService);

        assertTrue(c.equals(c2));
        assertTrue(c2.equals(c));
    }

    @Test
    public void verifySerializeAnHttpBasedServiceCredentialToJson() throws IOException {
        val credentialMetaDataWritten =
            new HttpBasedServiceCredential(new URL(CNN_URL),
                RegisteredServiceTestUtils.getRegisteredService(SOME_APP_URL));

        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);
        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, HttpBasedServiceCredential.class);
        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}
