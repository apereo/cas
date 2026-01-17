package org.apereo.cas.util;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
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
    void verifyNotValidRegex() {
        val notValidRegex = "***";
        assertFalse(RegexUtils.isValidRegex(notValidRegex));
    }

    @Test
    void verifyBlankValidRegex() {
        var pattern = RegexUtils.createPattern(StringUtils.EMPTY);
        assertNotNull(pattern);
        assertSame(RegexUtils.MATCH_NOTHING_PATTERN, pattern);
        pattern = RegexUtils.createPattern("********");
        assertNotNull(pattern);
        assertSame(RegexUtils.MATCH_NOTHING_PATTERN, pattern);
    }

    @Test
    void verifyNullRegex() {
        assertFalse(RegexUtils.isValidRegex(null));
    }

    @Test
    void verifyPatternCollection() {
        val patterns = List.of("^abc", "^\\d{3}\\w+");
        val result = RegexUtils.findFirst(patterns, List.of("hello", "world", "911/", "911Z")).get();
        assertEquals("911Z", result);
    }

    @Test
    void verifyPatternPerformance() {
        val pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        val stopWatch = new StopWatch();
        stopWatch.start();
        for (var i = 0; i < 10000; i++) {
            assertTrue(RegexUtils.isValidRegex(pattern));
            assertTrue(RegexUtils.createPattern(pattern).matcher("something@example.org").find());
        }
        stopWatch.stop();
        assertTrue(stopWatch.getTime() < 1000);
    }
}
