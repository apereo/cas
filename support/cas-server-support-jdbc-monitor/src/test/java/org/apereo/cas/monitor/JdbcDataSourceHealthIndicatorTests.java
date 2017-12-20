package org.apereo.cas.monitor;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JdbcDataSourceHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class JdbcDataSourceHealthIndicatorTests {

    private DataSource dataSource;

    @Before
    public void setUp() {
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/jpaTestApplicationContext.xml");
        this.dataSource = ctx.getBean("dataSource", DataSource.class);
    }

    @Test
    public void verifyObserve() {
        final JdbcDataSourceHealthIndicator monitor = new JdbcDataSourceHealthIndicator(5000, this.dataSource,
            "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        final Health status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }
}
