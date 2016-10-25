package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class DefaultRegisteredServiceMultifactorPolicyTest {

    private static final File JSON_FILE = new File("defaultRegisteredServiceMultifactorPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeADefaultRegisteredServiceMultifactorPolicyToJson() throws IOException {
        DefaultRegisteredServiceMultifactorPolicy policyWritten = new DefaultRegisteredServiceMultifactorPolicy();
        policyWritten.setPrincipalAttributeNameTrigger("trigger");
        policyWritten.setPrincipalAttributeValueToMatch("attribute");
        HashSet<String> providers = new HashSet<>();
        providers.add("providerOne");
        policyWritten.setMultifactorAuthenticationProviders(providers);

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceMultifactorPolicy policyRead = mapper.readValue(JSON_FILE, DefaultRegisteredServiceMultifactorPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}