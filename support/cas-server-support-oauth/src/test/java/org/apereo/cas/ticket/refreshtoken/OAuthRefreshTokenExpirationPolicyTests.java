package org.apereo.cas.ticket.refreshtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class OAuthRefreshTokenExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthRefreshTokenExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAnOAuthRefreshTokenExpirationPolicyToJson() throws IOException {
        final OAuthRefreshTokenExpirationPolicy policyWritten = new OAuthRefreshTokenExpirationPolicy(1234L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, OAuthRefreshTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
