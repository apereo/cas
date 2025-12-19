package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20JwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
class OAuth20JwtAccessTokenCipherExecutorTests extends AbstractOAuth20Tests {

    @Test
    void verifyOperation() {
        val cipher = new OAuth20JwtAccessTokenCipherExecutor(true, true);
        assertNotNull(cipher.getSigningKey());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKey());
        assertNotNull(cipher.getEncryptionKeySetting());
        assertNotNull(cipher.getName());
        val token = cipher.encode("example");
        assertEquals("example", cipher.decode(token));
    }
}
