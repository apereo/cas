package org.apereo.cas.logging;

import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleCloudAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Simple")
public class GoogleCloudAppenderTests {

    @Test
    public void verifyOperation() throws Exception {
        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(GoogleCloudAppender.class.getName());
        val appender = (GoogleCloudAppender) logger.getAppenders().get("GoogleCloudAppender");
        assertNotNull(appender);
        logger.info("Testing");
        Thread.sleep(5000);
    }
}
