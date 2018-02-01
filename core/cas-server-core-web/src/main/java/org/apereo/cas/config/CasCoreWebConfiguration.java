package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is {@link CasCoreWebConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreWebConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Load property files containing non-i18n fallback values
     * that should be exposed to Thyme templates.
     * keys in properties files added last will take precedence over the
     * internal cas_common_messages.properties.
     * Keys in regular messages bundles will override any of the common messages.
     *
     * @return PropertiesFactoryBean containing all common (non-i18n) messages
     */
    @Bean
    public PropertiesFactoryBean casCommonMessages() {
        final PropertiesFactoryBean properties = new PropertiesFactoryBean();
        final List<Resource> resourceList = new ArrayList<>();
        final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        resourceList.add(resourceLoader.getResource("classpath:/cas_common_messages.properties"));
        for (final String resourceName : casProperties.getMessageBundle().getCommonNames()) {
            final Resource resource = resourceLoader.getResource(resourceName);
            // resource existence unknown at this point, let PropertiesFactoryBean determine and log
            resourceList.add(resource);
        }
        properties.setLocations(resourceList.toArray(new Resource[]{}));
        properties.setSingleton(true);
        properties.setIgnoreResourceNotFound(true);
        return properties;
    }

    @RefreshScope
    @Bean
    public HierarchicalMessageSource messageSource(@Qualifier("casCommonMessages") final Properties casCommonMessages) {
        final CasReloadableMessageBundle bean = new CasReloadableMessageBundle();
        final MessageBundleProperties mb = casProperties.getMessageBundle();
        bean.setDefaultEncoding(mb.getEncoding());
        bean.setCacheSeconds(mb.getCacheSeconds());
        bean.setFallbackToSystemLocale(mb.isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(mb.isUseCodeMessage());
        bean.setBasenames(mb.getBaseNames().toArray(new String[]{}));
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

    @Bean
    public FactoryBean<UrlValidator> urlValidator() {
        final HttpClientProperties httpClient = this.casProperties.getHttpClient();
        final boolean allowLocalLogoutUrls = httpClient.isAllowLocalLogoutUrls();
        final String authorityValidationRegEx = httpClient.getAuthorityValidationRegEx();
        final boolean authorityValidationRegExCaseSensitive = httpClient.isAuthorityValidationRegExCaseSensitive();
        return new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx, authorityValidationRegExCaseSensitive);
    }
}
