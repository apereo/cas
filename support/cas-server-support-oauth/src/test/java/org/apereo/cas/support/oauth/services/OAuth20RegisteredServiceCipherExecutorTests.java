package org.apereo.cas.support.oauth.services;

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
@Tag("OAuth")
public class OAuth20RegisteredServiceCipherExecutorTests {
    @Test
    public void verifyOperation() {
        val cipher = new OAuth20RegisteredServiceCipherExecutor();
        val secret = RandomUtils.randomAlphanumeric(12);
        val encoded = cipher.encode(secret);
        assertNotNull(encoded);
        assertNotEquals(secret, encoded);
        val decoded = cipher.decode(encoded);
        assertEquals(secret, decoded);
    }
}
