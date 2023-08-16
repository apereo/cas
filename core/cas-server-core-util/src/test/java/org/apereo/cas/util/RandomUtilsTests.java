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
    public void beforeEach() {
        System.setProperty(RandomUtils.SYSTEM_PROPERTY_SECURE_RANDOM_ALG, StringUtils.EMPTY);
    }

    @Test
    void verifyUnknownAlg() throws Throwable {
        System.setProperty(RandomUtils.SYSTEM_PROPERTY_SECURE_RANDOM_ALG, "bad-algorithm");
        assertNotNull(RandomUtils.getNativeInstance());
    }

    @Test
    void verifyOperation() throws Throwable {
        val value = RandomUtils.generateSecureRandomId();
        assertNotNull(value);
    }

    @Test
    void verifyAlphaNumeric() throws Throwable {
        val value = RandomUtils.randomAlphanumeric(4, 8);
        assertNotNull(value);
    }

    @Test
    void verifyRandomAlphabetic() throws Throwable {
        val value = RandomUtils.randomAlphabetic(4, 8);
        assertNotNull(value);
    }

    @Test
    void verifyInt() throws Throwable {
        var value = RandomUtils.nextInt();
        assertNotNull(value);

        value = RandomUtils.nextInt(5, 5);
        assertNotNull(value);
    }

    @Test
    void verifyLong() throws Throwable {
        var value = RandomUtils.nextLong(3, 3);
        assertEquals(3, value);
    }

    @Test
    void verifyDouble() throws Throwable {
        var value = RandomUtils.nextDouble();
        assertNotNull(value);

        value = RandomUtils.nextDouble(5, 5);
        assertNotNull(value);
    }

    @Test
    void verifyValidation() throws Throwable {
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextDouble(10, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextDouble(-1, -1));

        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextInt(10, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextInt(-1, -1));

        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextLong(10, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.nextLong(-1, -1));
    }
}
