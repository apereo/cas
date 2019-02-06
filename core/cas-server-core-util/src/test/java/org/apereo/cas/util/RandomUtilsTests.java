package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RandomUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class RandomUtilsTests {
    @Test
    public void verifyOperation() {
        val value = RandomUtils.generateSecureRandomId();
        assertNotNull(value);
    }
}
