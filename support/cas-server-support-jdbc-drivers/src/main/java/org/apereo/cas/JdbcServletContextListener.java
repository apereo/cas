package org.apereo.cas;

import lombok.val;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is {@link JdbcServletContextListener} that properly
 * deregisters JDBC drivers in this context's ClassLoader.
 * <p>
 * Note that Slf4j logging does not work in contextDestroyed,
 * likely due to the fact that the logging framework as
 * shutdown and its context is destroyed sooner than this listener.
 * We need to stick to old-school logging method.
 *
 * @author leeyc0
 * @since 6.2.0
 */
@WebListener
public class JdbcServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {
        val logger = Logger.getLogger(CentralAuthenticationService.NAMESPACE);
        logger.fine("Unregistering JDBC drivers...");

        val cl = Thread.currentThread().getContextClassLoader();
        val drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            val driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    logger.fine("Attempting to deregister JDBC driver " + driver);
                    DriverManager.deregisterDriver(driver);
                } catch (final SQLException ex) {
                    logger.log(Level.WARNING, "Error deregistering JDBC driver ", ex);
                }
            } else {
                logger.fine("Not deregistering JDBC driver as it does not belong to this classloader: " + driver);
            }
        }
    }
}
