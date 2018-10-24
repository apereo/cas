package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

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
        val policyWritten = new RefuseRegisteredServiceProxyPolicy();

        MAPPER.writeValue(JSON_FILE, policyWritten);

        val policyRead = MAPPER.readValue(JSON_FILE, RefuseRegisteredServiceProxyPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
