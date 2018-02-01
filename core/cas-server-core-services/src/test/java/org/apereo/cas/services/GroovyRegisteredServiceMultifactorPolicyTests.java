package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyRegisteredServiceMultifactorPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class GroovyRegisteredServiceMultifactorPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "GroovyRegisteredServiceMultifactorPolicyTests.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();


    @Test
    public void checkDefaultPolicyConfig() {
        final GroovyRegisteredServiceMultifactorPolicy authz = new GroovyRegisteredServiceMultifactorPolicy();
        authz.setGroovyScript("classpath:mfapolicy.groovy");

        assertEquals(RegisteredServiceMultifactorPolicy.FailureModes.OPEN, authz.getFailureMode());
        assertEquals("Test", authz.getPrincipalAttributeNameTrigger());
        assertEquals("TestMatch", authz.getPrincipalAttributeValueToMatch());
        assertTrue(authz.getMultifactorAuthenticationProviders().contains("mfa-something"));
        assertTrue(authz.isBypassEnabled());
    }

    @Test
    public void verifySerializationToJson() throws IOException {
        final GroovyRegisteredServiceMultifactorPolicy authz = new GroovyRegisteredServiceMultifactorPolicy();
        authz.setGroovyScript("classpath:mfapolicy.groovy");
        MAPPER.writeValue(JSON_FILE, authz);
        final RegisteredServiceMultifactorPolicy strategyRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceMultifactorPolicy.class);
        assertEquals(authz, strategyRead);
        assertEquals("Test", strategyRead.getPrincipalAttributeNameTrigger());
    }
}
