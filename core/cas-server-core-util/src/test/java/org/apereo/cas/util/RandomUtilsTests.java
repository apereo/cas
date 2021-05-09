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
public class RandomUtilsTests {

    @BeforeEach
    public void beforeEach() {
        System.setProperty(RandomUtils.SYSTEM_PROPERTY_SECURE_RANDOM_ALG, StringUtils.EMPTY);
    }
    
    @Test
    public void verifyUnknownAlg() {
        System.setProperty(RandomUtils.SYSTEM_PROPERTY_SECURE_RANDOM_ALG, "bad-algorithm");
        assertNotNull(RandomUtils.getNativeInstance());
    }

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
    public void verifyLong() {
        var value = RandomUtils.nextLong(3, 3);
        assertEquals(3, value);
    }

    @Test
    public void verifyDouble() {
        var value = RandomUtils.nextDouble();
        assertNotNull(value);

        value = RandomUtils.nextDouble(5, 5);
        assertNotNull(value);
    }
}
