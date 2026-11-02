package org.apereo.cas.logging;

import module java.base;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
class CasAppenderTests {

    /**
     * Another test class in the same JVM may be starting the logger context at the same time;
     * {@link LoggerContext#start()} does not wait for a start already in progress, so the context
     * can still be on its default configuration, without the appenders of {@code log4j2-test.xml}.
     * Wait for the context to finish starting before looking up the appender.
     */
    @Test
    void verifyOperation() {
        val context = LoggerContext.getContext(false);
        await().atMost(Duration.ofSeconds(10)).until(context::isStarted);
        val logger = context.getLogger(CasAppender.class.getName());
        val appender = (CasAppender) logger.getAppenders().get("CasAppender");
        assertNotNull(appender);
        assertDoesNotThrow(() -> {
            new CasAppender("CasAppender2", mock(Configuration.class),
                AppenderRef.createAppenderRef("ref", Level.INFO, appender.getFilter()),
                appender.getFilter(), 10);
            logger.info("Testing CasAppender...");
        });
    }
}
