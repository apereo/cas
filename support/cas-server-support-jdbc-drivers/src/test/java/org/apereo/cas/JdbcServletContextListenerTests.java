package org.apereo.cas;

import module java.base;
import module java.sql;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.mock.web.MockServletContext;
import jakarta.servlet.ServletContextEvent;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcServletContextListenerTests}.
 * <p>
 * What is under test here is {@link DriverManager}, which is global to the JVM: one test registers
 * every driver on the classpath and the other expects none to be registered. Those cannot be true at
 * the same time, so the class declares the driver registry as a resource it holds exclusively.
 * <p>
 * Running them together does not merely fail, it hangs. {@code DriverManager.getDrivers()} initializes
 * the registry through a {@code ServiceLoader}, which loads and initializes driver classes while
 * holding the registry's lock, and {@code Class.forName} on a driver takes that class's initialization
 * lock and then asks the registry to register it. Two threads doing both at once deadlock.
 *
 * @author leeyc0
 * @since 6.2.0
 */
@Tag("Hibernate")
@ResourceLock("java.sql.DriverManager")
@SuppressWarnings("JdkObsolete")
class JdbcServletContextListenerTests {

    private final JdbcServletContextListener listener = new JdbcServletContextListener();

    /**
     * Initializing the context must not register drivers. The registry is emptied first rather than
     * assumed empty: merely asking for the drivers registers every one the classpath advertises, so
     * this test would otherwise pass only when it ran after the one below.
     */
    @Test
    void verifyContextInitialized() {
        listener.contextDestroyed(new ServletContextEvent(new MockServletContext()));
        listener.contextInitialized(null);
        assertFalse(DriverManager.getDrivers().hasMoreElements());
    }

    @Test
    void verifyContextDestroyed() throws Throwable {
        /* registers all drivers */
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Class.forName("org.postgresql.Driver");
        Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName("org.mariadb.jdbc.Driver");
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Class.forName("org.h2.Driver");
        listener.contextDestroyed(new ServletContextEvent(new MockServletContext()));
        assertFalse(DriverManager.getDrivers().hasMoreElements());
    }
}
