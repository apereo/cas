package org.apereo.cas.util;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RegexUtils}
 *
 * @author David Rodriguez
 * @since 5.1.0
 */
@Tag("Simple")
public class RegexUtilsTests {

    @Test
    public void verifyNotValidRegex() {
        val notValidRegex = "***";
        assertFalse(RegexUtils.isValidRegex(notValidRegex));
    }

    @Test
    public void verifyBlankValidRegex() {
        val pattern = RegexUtils.createPattern(StringUtils.EMPTY);
        assertNotNull(pattern);
        assertEquals(RegexUtils.MATCH_NOTHING_PATTERN, pattern);
    }

    @Test
    public void verifyNullRegex() {
        assertFalse(RegexUtils.isValidRegex(null));
    }
}
