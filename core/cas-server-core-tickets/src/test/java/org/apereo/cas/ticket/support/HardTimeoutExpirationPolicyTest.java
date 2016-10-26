package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HardTimeoutExpirationPolicyTest {

    private static final File JSON_FILE = new File("hardTimeoutExpirationPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        final HardTimeoutExpirationPolicy policyWritten = new HardTimeoutExpirationPolicy();

        mapper.writeValue(JSON_FILE, policyWritten);

        final ExpirationPolicy policyRead = mapper.readValue(JSON_FILE, HardTimeoutExpirationPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
