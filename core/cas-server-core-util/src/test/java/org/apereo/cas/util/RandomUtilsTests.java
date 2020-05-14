package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RandomUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Simple")
public class RandomUtilsTests {
    @Test
    public void verifyOperation() {
        val value = RandomUtils.generateSecureRandomId();
        assertNotNull(value);
    }

    @Test
    public void verifyAlphaNumeric() {
        val value = RandomUtils.randomAlphanumeric(4, 8);
        assertNotNull(value);
    }

    @Test
    public void verifyRandomAlphabetic() {
        val value = RandomUtils.randomAlphabetic(4, 8);
        assertNotNull(value);
    }

    @Test
    public void verifyInt() {
        var value = RandomUtils.nextInt();
        assertNotNull(value);

        value = RandomUtils.nextInt(5, 5);
        assertNotNull(value);
    }

    @Test
    public void verifyDouble() {
        var value = RandomUtils.nextDouble();
        assertNotNull(value);

        value = RandomUtils.nextDouble(5, 5);
        assertNotNull(value);
    }
}
