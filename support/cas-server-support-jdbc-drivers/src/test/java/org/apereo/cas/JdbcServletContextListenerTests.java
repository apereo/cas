package org.apereo.cas;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcServletContextListenerTests}.
 *
 * @author leeyc0
 * @since 6.2.0
 */
@Tag("Hibernate")
@SuppressWarnings("JdkObsolete")
class JdbcServletContextListenerTests {

    private final JdbcServletContextListener listener = new JdbcServletContextListener();

    @Test
    void verifyContextInitialized() {
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

        listener.contextDestroyed(null);
        assertFalse(DriverManager.getDrivers().hasMoreElements());
    }
}
