package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RefuseRegisteredServiceProxyPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "refuseRegisteredServiceProxyPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeARefuseRegisteredServiceProxyPolicyToJson() throws IOException {
        final RefuseRegisteredServiceProxyPolicy policyWritten = new RefuseRegisteredServiceProxyPolicy();

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceProxyPolicy policyRead = MAPPER.readValue(JSON_FILE, RefuseRegisteredServiceProxyPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
