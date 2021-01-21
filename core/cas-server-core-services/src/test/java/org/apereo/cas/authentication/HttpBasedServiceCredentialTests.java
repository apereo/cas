package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Authentication")
public class HttpBasedServiceCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "httpBasedServiceCredential.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyProperUrl() {
        assertEquals(CoreAuthenticationTestUtils.CONST_GOOD_URL,
            CoreAuthenticationTestUtils.getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }

    @Test
    public void verifyEqualsWithNull() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL);
        val c = new HttpBasedServiceCredential(new URL(CoreAuthenticationTestUtils.CONST_GOOD_URL), registeredService);
        assertNotEquals(c, null);
    }

    @Test
    public void verifyEqualsWithFalse() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL);
        val c = new HttpBasedServiceCredential(new URL(CoreAuthenticationTestUtils.CONST_GOOD_URL), registeredService);
        val c2 = new HttpBasedServiceCredential(new URL("http://www.msn.com"), registeredService);
        assertNotEquals(c2, c);
        assertNotEquals(new Object(), c);
    }

    @Test
    public void verifyEqualsWithTrue() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL);
        val callbackUrl = new URL(CoreAuthenticationTestUtils.CONST_GOOD_URL);
        val c = new HttpBasedServiceCredential(callbackUrl, registeredService);
        val c2 = new HttpBasedServiceCredential(callbackUrl, registeredService);

        assertEquals(c2, c);
        assertEquals(c, c2);
    }

    @Test
    public void verifySerializeAnHttpBasedServiceCredentialToJson() throws IOException {
        val credentialMetaDataWritten =
            new HttpBasedServiceCredential(new URL(CoreAuthenticationTestUtils.CONST_GOOD_URL),
                RegisteredServiceTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL));

        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);
        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, HttpBasedServiceCredential.class);
        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}
