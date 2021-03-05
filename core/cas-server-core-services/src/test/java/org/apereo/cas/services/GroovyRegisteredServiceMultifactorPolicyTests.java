package org.apereo.cas.services;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceMultifactorPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated This component is deprecated as of 6.2.0 and is scheduled to be removed.
 */
@Tag("Groovy")
@Deprecated(since = "6.2.0")
@SuppressWarnings("SuppressWarningsDeprecated")
public class GroovyRegisteredServiceMultifactorPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "GroovyRegisteredServiceMultifactorPolicyTests.json");
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();


    @Test
    public void checkDefaultPolicyConfig() {
        val authz = new GroovyRegisteredServiceMultifactorPolicy();
        authz.setGroovyScript("classpath:mfapolicy.groovy");

        assertEquals(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN, authz.getFailureMode());
        assertEquals("Test", authz.getPrincipalAttributeNameTrigger());
        assertEquals("TestMatch", authz.getPrincipalAttributeValueToMatch());
        assertTrue(authz.getMultifactorAuthenticationProviders().contains("mfa-something"));
        assertTrue(authz.isBypassEnabled());
        assertFalse(authz.isForceExecution());
        assertTrue(authz.isBypassTrustedDeviceEnabled());
        assertNull(authz.getBypassPrincipalAttributeName());
        assertNull(authz.getBypassPrincipalAttributeValue());
        assertNull(authz.getScript());
    }

    @Test
    public void verifySerializationToJson() throws IOException {
        val authz = new GroovyRegisteredServiceMultifactorPolicy();
        authz.setGroovyScript("classpath:mfapolicy.groovy");
        MAPPER.writeValue(JSON_FILE, authz);
        val strategyRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceMultifactorPolicy.class);
        assertEquals(authz, strategyRead);
        assertEquals("Test", strategyRead.getPrincipalAttributeNameTrigger());
    }
}
