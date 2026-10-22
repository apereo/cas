package org.apereo.cas.logging;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
        appender.stop();
    }

    @Test
    void verifyContextBecomesMessageAttributes() {
        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(SQSAppender.class.getName());
        IntStream.range(0, SQSAppender.MAX_MESSAGE_ATTRIBUTES + 5).forEach(idx -> ThreadContext.put("attribute" + idx, "value" + idx));
        ThreadContext.put("emptyAttribute", StringUtils.EMPTY);
        ThreadContext.put("AWS.reserved", "value");
        ThreadContext.put("invalid name", "value");
        try {
            assertDoesNotThrow(() -> logger.info("Testing with logging context..."));
        } finally {
            ThreadContext.clearAll();
        }
    }
}
