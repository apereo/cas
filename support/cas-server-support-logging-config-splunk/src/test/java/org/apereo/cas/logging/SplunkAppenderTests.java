package org.apereo.cas.logging;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SplunkAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Tag("Simple")
public class SplunkAppenderTests {
    @Test
    public void verifyAction() {
        LOGGER.info("Testing splunk appender");
        assertTrue(LOGGER.isInfoEnabled());
    }
}
