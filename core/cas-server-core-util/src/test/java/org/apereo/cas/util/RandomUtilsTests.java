package org.apereo.cas.util;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RandomUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Utility")
class RandomUtilsTests {

    @BeforeEach
    void beforeEach() {
        System.setProperty(RandomUtils.SYSTEM_PROPERTY_SECURE_RANDOM_ALG, StringUtils.EMPTY);
    }

    @Test
    void verifyUnknownAlg() {
        System.setProperty(RandomUtils.SYSTEM_PROPERTY_SECURE_RANDOM_ALG, "bad-algorithm");
        assertNotNull(RandomUtils.getNativeInstance());
    }

    @Test
    void verifyOperation() {
        val value = RandomUtils.generateSecureRandomId();
        assertNotNull(value);
    }

    @Test
    void verifyAlphaNumeric() {
        val value = RandomUtils.randomAlphanumeric(4, 8);
        assertNotNull(value);
    }

    @Test
    void verifyRandomAlphabetic() {
        val value = RandomUtils.randomAlphabetic(4, 8);
        assertNotNull(value);
    }

    @Test
    void verifyInt() {
        var value = RandomUtils.nextInt();
        assertTrue(value >= 0);

        value = RandomUtils.nextInt(5, 5);
        assertTrue(value >= 0);
    }

    @Test
    void verifyLong() {
        var value = RandomUtils.nextLong(3, 3);
        assertEquals(3, value);
    }

    @Test
    void verifyDouble() {
        var value = RandomUtils.nextDouble();
        assertTrue(value >= 0);

        value = RandomUtils.nextDouble(5, 5);
        assertTrue(value >= 0);
    }

    @Test
    void verifyValidation() {
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextDouble(10, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextDouble(-1, -1));

        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextInt(10, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextInt(-1, -1));

        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextLong(10, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextLong(-1, -1));
    }
}
