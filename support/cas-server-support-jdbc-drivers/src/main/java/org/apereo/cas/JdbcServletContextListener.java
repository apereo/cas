package org.apereo.cas;

import lombok.val;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This is {@link JdbcServletContextListener} that properly
 * deregisters JDBC drivers.
 *
 * Ref: https://github.com/apereo/cas/pull/4812
 * Note that Slf4j does not work in contextDestroyed.
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

    /*
     * Deregister JDBC drivers in this context's ClassLoader
     */
    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {
        val logger = Logger.getLogger("org.apereo.cas");
        logger.fine("Unregistering JdbcServletContextListener");

        val cl = Thread.currentThread().getContextClassLoader();
        val drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            val driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (final SQLException ex) {
                    logger.warning("Error deregistering JDBC driver " + ex);
                }
            } else {
                logger.fine("Not deregistering JDBC driver as it does not belong to this webapp's ClassLoader: " + driver);
            }
        }
    }
}
