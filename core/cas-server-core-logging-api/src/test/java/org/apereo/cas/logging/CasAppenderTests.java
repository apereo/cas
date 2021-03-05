package org.apereo.cas.logging;

import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CasAppenderTests {

    @Test
    public void verifyOperation() {
        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(CasAppender.class.getName());
        val appender = (CasAppender) logger.getAppenders().get("CasAppender");
        assertNotNull(appender);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                new CasAppender("CasAppender2", mock(Configuration.class),
                    AppenderRef.createAppenderRef("ref", Level.INFO, appender.getFilter()),
                    appender.getFilter());
                logger.info("Testing CasAppender...");
            }
        });
    }
}
