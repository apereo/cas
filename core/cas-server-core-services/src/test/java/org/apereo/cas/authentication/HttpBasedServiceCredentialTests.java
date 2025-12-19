package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Authentication")
class HttpBasedServiceCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "httpBasedServiceCredential.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyProperUrl() {
        assertEquals(CoreAuthenticationTestUtils.CONST_GOOD_URL,
            CoreAuthenticationTestUtils.getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }

    @Test
    void verifyEqualsWithNull() throws Throwable {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL);
        val c = new HttpBasedServiceCredential(new URI(CoreAuthenticationTestUtils.CONST_GOOD_URL).toURL(), registeredService);
        assertNotEquals(null, c);
    }

    @Test
    void verifyEqualsWithFalse() throws Throwable {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL);
        val c = new HttpBasedServiceCredential(new URI(CoreAuthenticationTestUtils.CONST_GOOD_URL).toURL(), registeredService);
        val c2 = new HttpBasedServiceCredential(new URI("http://www.msn.com").toURL(), registeredService);
        assertNotEquals(c2, c);
        assertNotEquals(new Object(), c);
    }

    @Test
    void verifyEqualsWithTrue() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL);
        val callbackUrl = new URI(CoreAuthenticationTestUtils.CONST_GOOD_URL).toURL();
        val c = new HttpBasedServiceCredential(callbackUrl, registeredService);
        val c2 = new HttpBasedServiceCredential(callbackUrl, registeredService);

        assertEquals(c2, c);
        assertEquals(c, c2);
    }

    @Test
    void verifySerializeAnHttpBasedServiceCredentialToJson() throws Throwable {
        val credentialMetaDataWritten =
            new HttpBasedServiceCredential(new URI(CoreAuthenticationTestUtils.CONST_GOOD_URL).toURL(),
                RegisteredServiceTestUtils.getRegisteredService(CoreAuthenticationTestUtils.CONST_TEST_URL));
        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);
        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, HttpBasedServiceCredential.class);
        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}
