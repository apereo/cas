package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.config.CasJdbcMonitorAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJdbcMonitorConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasJdbcMonitorAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class CasJdbcMonitorConfigurationTests {

    @Autowired
    @Qualifier("dataSourceHealthIndicator")
    private HealthIndicator dataSourceHealthIndicator;

    @Test
    void verifyOperation() {
        assertNotNull(dataSourceHealthIndicator);
    }
}
