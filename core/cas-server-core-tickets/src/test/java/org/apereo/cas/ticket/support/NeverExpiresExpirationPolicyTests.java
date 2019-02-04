package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class NeverExpiresExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "neverExpiresExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        val policyWritten = new NeverExpiresExpirationPolicy();

        MAPPER.writeValue(JSON_FILE, policyWritten);

        val policyRead = MAPPER.readValue(JSON_FILE, NeverExpiresExpirationPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
