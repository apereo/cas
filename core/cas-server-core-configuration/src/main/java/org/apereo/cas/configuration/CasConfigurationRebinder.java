package org.apereo.cas.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This is {@link CasConfigurationRebinder}. Handles events issued by the configuration server
 * and rebinds {@link CasConfigurationProperties} back into the context.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("casConfigurationRebinder")
public class CasConfigurationRebinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationRebinder.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigurationPropertiesBindingPostProcessor binder;

    /**
     * Handle refresh event when issued to this CAS server locally.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final EnvironmentChangeEvent event) {
        LOGGER.debug("Received event {}", event);
        rebindCasConfigurationProperties();
    }

    /**
     * Handle refresh event when issued by the cloud bus.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final RefreshRemoteApplicationEvent event) {
        LOGGER.debug("Received event {}", event);
        rebindCasConfigurationProperties();
    }

    /**
     * Rebind cas configuration properties.
     */
    public void rebindCasConfigurationProperties() {
        final Map<String, CasConfigurationProperties> map =
                this.applicationContext.getBeansOfType(CasConfigurationProperties.class);
        final String name = map.keySet().iterator().next();
        LOGGER.debug("Reloading CAS configuration via {}", name);
        final Object e = this.applicationContext.getBean(name);
        this.binder.postProcessBeforeInitialization(e, name);
        final Object bean = this.applicationContext.getAutowireCapableBeanFactory().initializeBean(e, name);
        this.applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        LOGGER.info("Reloaded CAS configuration {}", name);
    }
}
