package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RegisteredServiceCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Cipher")
class OAuth20RegisteredServiceCipherExecutorTests {
    @Test
    void verifyOperation() {
        val cipher = new OAuth20RegisteredServiceCipherExecutor();
        val secret = RandomUtils.randomAlphanumeric(12);
        val encoded = cipher.encode(secret);
        assertNotNull(encoded);
        assertNotEquals(secret, encoded);
        val decoded = cipher.decode(encoded);
        assertEquals(secret, decoded);
    }

    @Test
    void verifyAlgorithm() {
        val encryptionKey = "wXXRBPCvrRjXmGzhbkAzE3U_I4lBzFTy0sNtq21U5Cw";
        val signingKey = "LA7Ud84i_k-gOvyBgWB2FUBuYQipuBzY0jhqYMD3mAUFQb405tEyj8ACqq9i50R-TsyAFlNFCWRIuoQpsdKFlg";
        val cipher = new OAuth20RegisteredServiceCipherExecutor(encryptionKey, signingKey, "A128CBC-HS256");
        val encoded = cipher.encode("secret");
        assertNotNull(encoded);
        val decoded = cipher.decode(encoded);
        assertEquals("secret", decoded);
    }
}
