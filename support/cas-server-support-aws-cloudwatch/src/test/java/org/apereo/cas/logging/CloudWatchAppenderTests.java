package org.apereo.cas.logging;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.core.SdkSystemSetting;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CloudWatchAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CloudWatchAppenderTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Test
    void verifyOperation() throws Throwable {
        val config = new ClassPathResource("log4j2-test.xml");
        val context = LoggerContext.getContext(CloudWatchAppenderTests.class.getClassLoader(), false, config.getURI());
        val logger = context.getLogger(CloudWatchAppender.class.getName());
        val appender = (CloudWatchAppender) logger.getAppenders().get("CloudWatchAppender");
        assertNotNull(appender);

        assertDoesNotThrow(() -> {
            val event = mock(LogEvent.class);
            when(event.getMessage()).thenReturn(new SimpleMessage());
            appender.append(event);
            IntStream.range(1, 20).forEach(idx -> logger.info("Testing CloudWatchAppender [{}]...", idx));
        });
        appender.stop();
    }
}
