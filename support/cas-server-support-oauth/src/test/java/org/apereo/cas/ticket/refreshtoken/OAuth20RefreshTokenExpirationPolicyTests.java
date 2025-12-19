package org.apereo.cas.ticket.refreshtoken;

import module java.base;
import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RefreshTokenExpirationPolicyTests}.
 *
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.ticket.track-descendant-tickets=true")
@Tag("OAuthToken")
class OAuth20RefreshTokenExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "OAuth20RefreshTokenExpirationPolicy.json");

    @Test
    void verifyRefreshTokenExpiryWhenTgtIsExpired() throws Throwable {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);

        assertFalse(rt.isExpired(), "Refresh token should not be expired");
        tgt.markTicketExpired();
        assertTrue(rt.isExpired(), "Refresh token should be expired when TGT is expired");
    }

    @Test
    void verifyFails() throws Throwable {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);
        assertTrue(rt.getExpirationPolicy().isExpired(null));
    }

    @Test
    void verifySerializeAnOAuthRefreshTokenExpirationPolicyToJson() throws Throwable {
        val policyWritten = new OAuth20RefreshTokenExpirationPolicy(1234L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, OAuth20RefreshTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
