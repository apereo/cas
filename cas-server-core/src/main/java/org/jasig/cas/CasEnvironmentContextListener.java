package org.jasig.cas;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Formatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A context listener that reports back the CAS application
 * deployment environment info. Details such as CAS versin,
 * Java/OS info as well as the server container info are logged.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("casEnvironmentContextListener")
public final class CasEnvironmentContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasEnvironmentContextListener.class);

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    /**
     * Instantiates a new Cas environment context listener.
     */
    public CasEnvironmentContextListener() {
        super();
        LOGGER.debug("[{}] initialized...", CasEnvironmentContextListener.class.getSimpleName());
    }

    /**
     * Logs environment info by collecting
     * details on the java and os deployment
     * environment. Data is logged at DEBUG
     * level.
     */
    @PostConstruct
    public void logEnvironmentInfo() {
        if (!INITIALIZED.get()) {
            LOGGER.info(collectEnvironmentInfo());
            INITIALIZED.set(true);
        }
    }

    /**
     * Collect environment info with
     * details on the java and os deployment
     * versions.
     *
     * @return environment info
     */
    private String collectEnvironmentInfo() {
        final Properties properties = System.getProperties();
        try (final Formatter formatter = new Formatter()) {
            formatter.format("\n******************** Welcome to CAS *******************\n");
            formatter.format("CAS Version: %s\n", CasVersion.getVersion());
            formatter.format("Build Date/Time: %s\n", CasVersion.getDateTime());
            formatter.format("Java Home: %s\n", properties.get("java.home"));
            formatter.format("Java Vendor: %s\n", properties.get("java.vendor"));
            formatter.format("Java Version: %s\n", properties.get("java.version"));
            formatter.format("OS Architecture: %s\n", properties.get("os.arch"));
            formatter.format("OS Name: %s\n", properties.get("os.name"));
            formatter.format("OS Version: %s\n", properties.get("os.version"));
            formatter.format("*******************************************************\n");
            return formatter.toString();
        }
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append(collectEnvironmentInfo());
        return builder.toString();
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();
        final ApplicationContext ctx =
            WebApplicationContextUtils.getWebApplicationContext(servletContext);

        LOGGER.info("[{}] has loaded the CAS servlet application context: {}",
            servletContext.getServerInfo(), ctx);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {}
}
