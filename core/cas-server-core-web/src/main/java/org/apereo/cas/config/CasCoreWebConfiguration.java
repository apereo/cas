package org.apereo.cas.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasCoreWebConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreWebConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Get messages from one or more property files containing values
     * that should be exposed to Thyme templates but don't need i18n.
     * Implementors could add custom-common-messages.properties to their overlay and
     * keys in that will take precedence over the internal common-messages.properties.
     * Keys in regular messages bundles will override any of the common messages.
     * @return PropertiesFactoryBean containing all common (non-i18n) messages
     */
    @Bean
    public PropertiesFactoryBean casCommonMessages() {
        final PropertiesFactoryBean properties = new PropertiesFactoryBean();
        final Resource[] propertyResources = new Resource[] {
            new ClassPathResource("/common-messages.properties"),
            new ClassPathResource("/custom-common-messages.properties"),
        };
        properties.setLocations(propertyResources);
        properties.setSingleton(true);
        properties.setIgnoreResourceNotFound(false);
        return properties;
    }

    @RefreshScope
    @Bean
    public HierarchicalMessageSource messageSource(@Qualifier("casCommonMessages") final Properties casCommonMessages) {
        final CasReloadableMessageBundle bean = new CasReloadableMessageBundle();
        bean.setDefaultEncoding(casProperties.getMessageBundle().getEncoding());
        bean.setCacheSeconds(casProperties.getMessageBundle().getCacheSeconds());
        bean.setFallbackToSystemLocale(casProperties.getMessageBundle().isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(casProperties.getMessageBundle().isUseCodeMessage());
        bean.setBasenames(casProperties.getMessageBundle().getBaseNames());
        bean.setCommonMessages(casCommonMessages);
        return bean;
    }

    @Autowired
    @Bean
    public ArgumentExtractor argumentExtractor(final List<ServiceFactoryConfigurer> configurers) {
        final List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList = new ArrayList<>();
        configurers.forEach(c -> serviceFactoryList.addAll(c.buildServiceFactories()));
        return new DefaultArgumentExtractor(serviceFactoryList);
    }
}
