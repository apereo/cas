package org.apereo.cas.web;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is {@link CasServletContextListenerConfigurationBean} that properly
 * deregisters JDBC drivers.
 *
 * @author leeyc0
 * @since 6.2.0
 */
@Configuration("casServletContextListenerConfigurationBean")
@Slf4j
public class CasServletContextListenerConfigurationBean {
    @Bean
    ServletListenerRegistrationBean<ServletContextListener> servletListener() {
        val srb = new ServletListenerRegistrationBean<ServletContextListener>();
        srb.setListener(new CasServletContextListener());
        return srb;
    }

    private static class CasServletContextListener implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent sce) {
            LOGGER.debug("Registered CasServletContextListener");
        }

        @Override
        public final void contextDestroyed(ServletContextEvent sce) {
            /* ... First close any background tasks which may be using the DB ...
               ... Then close any DB connection pools ...

               Now deregister JDBC drivers in this context's ClassLoader:
               Get the webapp's ClassLoader */
            val cl = Thread.currentThread().getContextClassLoader();
            val drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                val driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == cl) {
                    /* This driver was registered by the webapp's ClassLoader, so deregister it: */
                    try {
                        LOGGER.debug("Deregistering JDBC driver {}", driver);
                        DriverManager.deregisterDriver(driver);
                    } catch (final SQLException ex) {
                        LOGGER.debug("Error deregistering JDBC driver {}", ex);
                    }
                } else {
                    LOGGER.debug("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
                }
            }
        }
    }
}
