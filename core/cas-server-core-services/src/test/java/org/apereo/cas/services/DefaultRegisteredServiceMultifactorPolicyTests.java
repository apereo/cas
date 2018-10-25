package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultRegisteredServiceMultifactorPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultRegisteredServiceMultifactorPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeADefaultRegisteredServiceMultifactorPolicyToJson() throws IOException {
        val policyWritten = new DefaultRegisteredServiceMultifactorPolicy();
        policyWritten.setPrincipalAttributeNameTrigger("trigger");
        policyWritten.setPrincipalAttributeValueToMatch("attribute");
        val providers = new HashSet<String>();
        providers.add("providerOne");
        policyWritten.setMultifactorAuthenticationProviders(providers);

        MAPPER.writeValue(JSON_FILE, policyWritten);

        val policyRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceMultifactorPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
