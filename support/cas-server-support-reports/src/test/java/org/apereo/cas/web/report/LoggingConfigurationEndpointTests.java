package org.apereo.cas.web.report;

import org.apereo.cas.logging.web.LoggingConfigurationEndpoint;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LoggingConfigurationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "management.endpoint.logging-config.enabled=true",
    "logging.config=file:${java.io.tmpdir}/log4j2.xml"
})
@Tag("ActuatorEndpoint")
public class LoggingConfigurationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("loggingConfigurationEndpoint")
    private LoggingConfigurationEndpoint loggingConfigurationEndpoint;

    @BeforeAll
    public static void setup() throws Exception {
        val content = IOUtils.toString(new ClassPathResource("log4j2-test.xml.template").getInputStream(), StandardCharsets.UTF_8);
        try (val writer = new FileWriter(new File(FileUtils.getTempDirectory(), "log4j2.xml"), StandardCharsets.UTF_8)) {
            IOUtils.write(content, writer);
            writer.flush();
        }
    }

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
