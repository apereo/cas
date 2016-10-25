package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RefuseRegisteredServiceProxyPolicyTest {

    private static final File JSON_FILE = new File("refuseRegisteredServiceProxyPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeARefuseRegisteredServiceProxyPolicyToJson() throws IOException {
        final RefuseRegisteredServiceProxyPolicy policyWritten = new RefuseRegisteredServiceProxyPolicy();

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceProxyPolicy policyRead = mapper.readValue(JSON_FILE, RefuseRegisteredServiceProxyPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}