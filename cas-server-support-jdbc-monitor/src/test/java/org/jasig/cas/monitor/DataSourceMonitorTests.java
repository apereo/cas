package org.jasig.cas.monitor;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link DataSourceMonitor}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class DataSourceMonitorTests {

    private DataSource dataSource;

    @Before
    public void setup() {
        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaTestApplicationContext.xml");
        this.dataSource = ctx.getBean("dataSource", DataSource.class);
    }

    @Test
    public void verifyObserve() throws Exception {
        final DataSourceMonitor monitor = new DataSourceMonitor(this.dataSource);
        monitor.setExecutor(Executors.newSingleThreadExecutor());
        monitor.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.OK, status.getCode());
    }
}
