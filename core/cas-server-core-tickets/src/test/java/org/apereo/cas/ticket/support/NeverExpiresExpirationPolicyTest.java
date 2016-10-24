package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class NeverExpiresExpirationPolicyTest {

    private static final File JSON_FILE = new File("neverExpiresExpirationPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        NeverExpiresExpirationPolicy policyWritten = new NeverExpiresExpirationPolicy();

        mapper.writeValue(JSON_FILE, policyWritten);

        final ExpirationPolicy policyRead = mapper.readValue(JSON_FILE, NeverExpiresExpirationPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}