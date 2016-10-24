package org.apereo.cas.ticket.accesstoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OAuthAccessTokenExpirationPolicyTest {

    private static final File JSON_FILE = new File("oAuthAccessTokenExpirationPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAnOAuthAccessTokenExpirationPolicyToJson() throws IOException {
        OAuthAccessTokenExpirationPolicy policyWritten = new OAuthAccessTokenExpirationPolicy(1234L, 5678L);

        mapper.writeValue(JSON_FILE, policyWritten);

        final ExpirationPolicy policyRead = mapper.readValue(JSON_FILE, OAuthAccessTokenExpirationPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}