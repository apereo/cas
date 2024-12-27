package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJaegerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasJaegerAutoConfiguration.class
})
@Tag("Metrics")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class CasJaegerConfigurationTests {

    @Autowired
    @Qualifier("jaegerGrpcHttpSpanExporter")
    private SpanExporter jaegerGrpcHttpSpanExporter;

    @Test
    void verifyOperation() {
        assertNotNull(jaegerGrpcHttpSpanExporter);
    }
}
