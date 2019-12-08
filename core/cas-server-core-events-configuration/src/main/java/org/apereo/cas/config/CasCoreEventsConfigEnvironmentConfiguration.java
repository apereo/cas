package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.listener.CasConfigurationEventListener;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreEventsConfigEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreEventsConfigEnvironmentConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreEventsConfigEnvironmentConfiguration {

    @Autowired
    @Qualifier("configurationPropertiesEnvironmentManager")
    private ObjectProvider<CasConfigurationPropertiesEnvironmentManager> manager;

    @Autowired
    private ObjectProvider<ConfigurationPropertiesBindingPostProcessor> binder;

    @Autowired
    private ObjectProvider<ContextRefresher> contextRefresher;

    @Autowired
    private ApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "casConfigurationEventListener")
    @Bean
    public CasConfigurationEventListener casConfigurationEventListener() {
        return new CasConfigurationEventListener(manager.getObject(), binder.getObject(),
            contextRefresher.getObject(), applicationContext);
    }

}
