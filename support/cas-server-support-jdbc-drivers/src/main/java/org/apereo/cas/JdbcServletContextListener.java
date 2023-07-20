package org.apereo.cas;

import lombok.val;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.DriverManager;
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
@SuppressWarnings("JdkObsolete")
public class JdbcServletContextListener implements ServletContextListener {
    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {
        val logger = Logger.getLogger(CentralAuthenticationService.NAMESPACE);
        logger.fine("Unregistering JDBC drivers...");

        val cl = Thread.currentThread().getContextClassLoader();
        val drivers = DriverManager.getDrivers().asIterator();

        while (drivers.hasNext()) {
            val driver = drivers.next();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    logger.fine("Attempting to deregister JDBC driver " + driver);
                    DriverManager.deregisterDriver(driver);
                } catch (final Exception ex) {
                    logger.log(Level.WARNING, "Error deregistering JDBC driver ", ex);
                }
            } else {
                logger.fine("Not deregistering JDBC driver as it does not belong to this classloader: " + driver);
            }
        }
    }
}
