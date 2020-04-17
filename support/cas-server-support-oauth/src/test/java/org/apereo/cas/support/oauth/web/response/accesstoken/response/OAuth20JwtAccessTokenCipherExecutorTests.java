package org.apereo.cas.support.oauth.web.response.accesstoken.response;

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
public class OAuth20JwtAccessTokenCipherExecutorTests extends AbstractOAuth20Tests {

    @Test
    public void verifyOperation() {
        val c = new OAuth20JwtAccessTokenCipherExecutor(true, true);
        assertNotNull(c.getSigningKey());
        assertNotNull(c.getSigningKeySetting());
        assertNotNull(c.getSecretKeyEncryptionKey());
        assertNotNull(c.getEncryptionKeySetting());
        assertNotNull(c.getName());
        val token = c.encode("example");
        assertEquals("example", c.decode(token));
    }
}
