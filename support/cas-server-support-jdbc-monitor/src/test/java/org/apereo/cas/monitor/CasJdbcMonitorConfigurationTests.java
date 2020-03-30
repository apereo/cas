package org.apereo.cas.monitor;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.config.CasJdbcMonitorConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJdbcMonitorConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasJdbcMonitorConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("JDBC")
public class CasJdbcMonitorConfigurationTests {

    @Autowired
    @Qualifier("dataSourceHealthIndicator")
    private HealthIndicator dataSourceHealthIndicator;

    @Test
    public void verifyOperation() {
        assertNotNull(dataSourceHealthIndicator);
    }
}
