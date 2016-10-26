package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 3.0
 */
public class AlwaysExpiresExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "alwaysExpiresExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAnAlwaysExpiresExpirationPolicyToJson() throws IOException {
        final AlwaysExpiresExpirationPolicy policyWritten = new AlwaysExpiresExpirationPolicy();

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, AlwaysExpiresExpirationPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
