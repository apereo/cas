package org.apereo.cas.util.spring;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Misagh Moayyed
 * An implementation of {@link ApplicationContextAware} that statically
 * holds the application context
 * @since 3.0.0.
 */
public class ApplicationContextProvider implements ApplicationContextAware, ResourceLoaderAware {
    private static ApplicationContext CONTEXT;
    private static ResourceLoader RESOURCE_LOADER;

    public static ApplicationContext getApplicationContext() {
        return CONTEXT;
    }

    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        CONTEXT = ctx;
    }

    public ConfigurableApplicationContext getConfigurableApplicationContext() {
        return (ConfigurableApplicationContext) CONTEXT;
    }

    /**
     * Gets resource loader.
     *
     * @return the resource loader
     */
    public static ResourceLoader getResourceLoader() {
        return RESOURCE_LOADER;
    }

    @Override
    public void setResourceLoader(final org.springframework.core.io.ResourceLoader resourceLoader) {
        RESOURCE_LOADER = resourceLoader;
    }

    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
        return getConfigurableApplicationContext().getAutowireCapableBeanFactory();
    }
}
