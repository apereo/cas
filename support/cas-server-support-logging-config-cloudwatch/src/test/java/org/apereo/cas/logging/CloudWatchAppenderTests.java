package org.apereo.cas.logging;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CloudWatchAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfPortOpen(port = 4586)
@Tag("AmazonWebServices")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CloudWatchAppenderTests {
    @Test
    public void verifyOperation() {
        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(CloudWatchAppender.class.getName());
        val appender = (CloudWatchAppender) logger.getAppenders().get("CloudWatchAppender");
        assertNotNull(appender);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                IntStream.range(1, 20).forEach(idx -> logger.info("Testing CloudWatchAppender [{}]...", idx));
            }
        });
    }
}
