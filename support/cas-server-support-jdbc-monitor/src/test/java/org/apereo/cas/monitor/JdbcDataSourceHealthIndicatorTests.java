package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JdbcDataSourceHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class JdbcDataSourceHealthIndicatorTests {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private CasConfigurationProperties casProperties;

    private DataSource dataSource;

    @Before
    public void setUp() {
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/jpaTestApplicationContext.xml");
        this.dataSource = ctx.getBean("dataSource", DataSource.class);
    }

    @Test
    public void verifyObserve() {
        final JdbcDataSourceHealthIndicator monitor = new JdbcDataSourceHealthIndicator(5000,
            this.dataSource, this.executor,
            "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        final Health status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }
}
