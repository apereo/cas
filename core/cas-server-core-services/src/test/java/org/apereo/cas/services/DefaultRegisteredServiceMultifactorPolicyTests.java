package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultRegisteredServiceMultifactorPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultRegisteredServiceMultifactorPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeADefaultRegisteredServiceMultifactorPolicyToJson() throws IOException {
        final DefaultRegisteredServiceMultifactorPolicy policyWritten = new DefaultRegisteredServiceMultifactorPolicy();
        policyWritten.setPrincipalAttributeNameTrigger("trigger");
        policyWritten.setPrincipalAttributeValueToMatch("attribute");
        final HashSet<String> providers = new HashSet<>();
        providers.add("providerOne");
        policyWritten.setMultifactorAuthenticationProviders(providers);

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceMultifactorPolicy policyRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceMultifactorPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
