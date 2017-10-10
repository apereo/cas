package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Map;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebApplicationServletInitializer extends SpringBootServletInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasWebApplicationServletInitializer.class);
    
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        final Map<String, Object> properties = CasEmbeddedContainerUtils.getRuntimeProperties(Boolean.FALSE);
        return builder
                .sources(CasWebApplication.class)
                .properties(properties)
                .banner(CasEmbeddedContainerUtils.getCasBannerInstance());
    }

    /**
     * {@inheritDoc}
     * Overrides the parent method to remove a pre-mature logging operation via (<code>LogFactory#getLog(getClass());</code>)
     * which forces log4j to locate the configuration file from its known locations (i.e. classpath)
     * before Spring Boot has had a chance to consult properties, etc.
     * @param servletContext the context
     */
    @Override
    public void onStartup(final ServletContext servletContext) {
        final WebApplicationContext rootAppContext = this.createRootApplicationContext(servletContext);
        if (rootAppContext != null) {
            servletContext.addListener(new ContextLoaderListener(rootAppContext) {
                public void contextInitialized(final ServletContextEvent event) {
                }
            });
        } else {
            LOGGER.debug("No ContextLoaderListener registered, as createRootApplicationContext() did not return an application context");
        }
    }
}
