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
@Tag("Utility")
class RegexUtilsTests {

    @Test
    void verifyNotValidRegex() throws Throwable {
        val notValidRegex = "***";
        assertFalse(RegexUtils.isValidRegex(notValidRegex));
    }

    @Test
    void verifyBlankValidRegex() throws Throwable {
        var pattern = RegexUtils.createPattern(StringUtils.EMPTY);
        assertNotNull(pattern);
        assertSame(RegexUtils.MATCH_NOTHING_PATTERN, pattern);
        pattern = RegexUtils.createPattern("********");
        assertNotNull(pattern);
        assertSame(RegexUtils.MATCH_NOTHING_PATTERN, pattern);
    }

    @Test
    void verifyNullRegex() throws Throwable {
        assertFalse(RegexUtils.isValidRegex(null));
    }
}
