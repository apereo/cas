package org.apereo.cas.monitor;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JdbcDataSourceHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("JDBC")
class JdbcDataSourceHealthIndicatorTests {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private DataSource dataSource;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    void initialize() {
        val props = casProperties.getMonitor().getJdbc();
        this.dataSource = JpaBeans.newDataSource(props);
    }

    @Test
    void verifyObserve() {
        val monitor = new JdbcDataSourceHealthIndicator(5000,
            this.dataSource, this.executor,
            "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        val status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }

    @Test
    void verifyBadQuery() {
        val monitor = new JdbcDataSourceHealthIndicator(5000,
            this.dataSource, this.executor,
            "SELECT 1 FROM XYZ");
        val status = monitor.health();
        assertNotEquals(Status.UP, status.getStatus());
    }
}
