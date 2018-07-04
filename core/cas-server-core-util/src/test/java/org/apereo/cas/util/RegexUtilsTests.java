package org.apereo.cas.util;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link RegexUtils}
 *
 * @author David Rodriguez
 * @since 5.1.0
 */
@Slf4j
public class RegexUtilsTests {

    @Test
    public void verifyNotValidRegex() {
        val notValidRegex = "***";

        assertFalse(RegexUtils.isValidRegex(notValidRegex));
    }

    @Test
    public void verifyNullRegex() {
        final String nullRegex = null;

        assertFalse(RegexUtils.isValidRegex(nullRegex));
    }
}
