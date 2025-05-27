package org.apereo.cas.logging;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SQSAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
class SQSAppenderTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(SQSAppender.class.getName());
        val appender = (SQSAppender) logger.getAppenders().get("SQSAppender");
        assertNotNull(appender);
        assertDoesNotThrow(() -> IntStream.range(1, 20)
            .forEach(idx -> logger.info("Testing [{}]...", idx)));
        Thread.sleep(5_000);
        appender.stop();
    }
}
