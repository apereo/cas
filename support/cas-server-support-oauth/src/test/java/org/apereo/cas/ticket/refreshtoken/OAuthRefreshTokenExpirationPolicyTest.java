package org.apereo.cas.ticket.refreshtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OAuthRefreshTokenExpirationPolicyTest {

    private static final File JSON_FILE = new File("oAuthRefreshTokenExpirationPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAnOAuthRefreshTokenExpirationPolicyToJson() throws IOException {
        final OAuthRefreshTokenExpirationPolicy policyWritten = new OAuthRefreshTokenExpirationPolicy(1234L);

        mapper.writeValue(JSON_FILE, policyWritten);

        final ExpirationPolicy policyRead = mapper.readValue(JSON_FILE, RememberMeDelegatingExpirationPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
