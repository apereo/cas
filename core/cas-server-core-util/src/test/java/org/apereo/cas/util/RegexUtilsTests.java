package org.apereo.cas.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link RegexUtils}
 *
 * @author David Rodriguez
 * @since 5.1.0
 */
public class RegexUtilsTests {

    @Test
    public void verifyNotValidRegex() throws Exception {
        final String notValidRegex = "***";

        assertFalse(RegexUtils.isValidRegex(notValidRegex));
    }

    @Test
    public void verifyNullRegex() throws Exception {
        final String nullRegex = null;

        assertFalse(RegexUtils.isValidRegex(nullRegex));
    }
}
