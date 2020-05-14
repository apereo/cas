package org.apereo.cas.web.report;

import org.apereo.cas.logging.web.LoggingConfigurationEndpoint;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LoggingConfigurationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties =
    "management.endpoint.loggingConfig.enabled=true"
)
@Tag("Simple")
public class LoggingConfigurationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("loggingConfigurationEndpoint")
    private LoggingConfigurationEndpoint loggingConfigurationEndpoint;

    @Test
    public void verifyOperation() {
        assertNotNull(loggingConfigurationEndpoint);
        val configuration = loggingConfigurationEndpoint.configuration();
        assertNotNull(configuration);
        assertTrue(configuration.containsKey("loggers"));
        assertTrue(configuration.containsKey("activeLoggers"));
    }

    @Test
    public void verifyUpdateOperation() {
        assertNotNull(loggingConfigurationEndpoint);
        assertDoesNotThrow(() ->
            loggingConfigurationEndpoint.updateLoggerLevel("org.apereo", "warn", false));
    }
}
