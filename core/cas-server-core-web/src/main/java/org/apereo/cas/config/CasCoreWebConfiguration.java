package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

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

    @RefreshScope
    @Bean
    public HierarchicalMessageSource messageSource() {
        final CasReloadableMessageBundle bean = new CasReloadableMessageBundle();
        bean.setDefaultEncoding(casProperties.getMessageBundle().getEncoding());
        bean.setCacheSeconds(casProperties.getMessageBundle().getCacheSeconds());
        bean.setFallbackToSystemLocale(casProperties.getMessageBundle().isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(casProperties.getMessageBundle().isUseCodeMessage());
        bean.setBasenames(casProperties.getMessageBundle().getBaseNames());
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
