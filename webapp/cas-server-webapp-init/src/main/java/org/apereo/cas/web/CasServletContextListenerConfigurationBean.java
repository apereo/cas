package org.apereo.cas.web;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Configuration
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
            // ... First close any background tasks which may be using the DB ...
            // ... Then close any DB connection pools ...

            // Now deregister JDBC drivers in this context's ClassLoader:
            // Get the webapp's ClassLoader
            val cl = Thread.currentThread().getContextClassLoader();
            // Loop through all drivers
            val drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                val driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == cl) {
                    // This driver was registered by the webapp's ClassLoader, so deregister it:
                    try {
                        LOGGER.debug("Deregistering JDBC driver {}", driver);
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException ex) {
                        LOGGER.debug("Error deregistering JDBC driver {}", ex);
                    }
                } else {
                    // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                    LOGGER.debug("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
                }
            }
        }
    }
}
