package org.apereo.cas.support.oauth.services;

import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
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
        val secret = RandomStringUtils.randomAlphanumeric(12);
        val encoded = cipher.encode(secret);
        assertNotNull(encoded);
        assertNotEquals(secret, encoded);
        val decoded = cipher.decode(encoded);
        assertEquals(secret, decoded);
    }
}
