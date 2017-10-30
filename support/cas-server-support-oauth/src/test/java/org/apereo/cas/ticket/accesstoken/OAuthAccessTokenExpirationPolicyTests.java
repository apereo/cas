package org.apereo.cas.ticket.accesstoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthAccessTokenExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthAccessTokenExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAnOAuthAccessTokenExpirationPolicyToJson() throws IOException {
        final OAuthAccessTokenExpirationPolicy policyWritten = new OAuthAccessTokenExpirationPolicy(1234L, 5678L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, OAuthAccessTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
