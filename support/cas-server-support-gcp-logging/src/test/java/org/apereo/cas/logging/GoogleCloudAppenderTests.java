package org.apereo.cas.logging;

import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.text.MessageSanitizer;

import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    public void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MessageSanitizer.disabled(), MessageSanitizer.BEAN_NAME);

        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(GoogleCloudAppender.class.getName());
        val appender = (GoogleCloudAppender) logger.getAppenders().get("GoogleCloudAppender");
        assertNotNull(appender);
        logger.info("This is an INFO log message here");
        logger.warn("This is a WARNING log message here");
        logger.info("This is a parametrized message with a POJO [{}]",
            new Pojo(UUID.randomUUID().toString(), 1984L));
        logger.info(new Pojo("Payload is an object here", 1984L));
        logger.info(Map.of("application", "CAS", "org", "Apereo"));
        context.stop(5, TimeUnit.SECONDS);
    }

    public record Pojo(String name, Long id) {
    }
}
