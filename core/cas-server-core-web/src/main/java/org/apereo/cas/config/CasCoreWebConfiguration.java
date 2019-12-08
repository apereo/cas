package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreWebConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreWebConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
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
        val properties = new PropertiesFactoryBean();
        val resourceLoader = new DefaultResourceLoader();
        val commonNames = casProperties.getMessageBundle().getCommonNames();

        val resourceList = commonNames
            .stream()
            .map(resourceLoader::getResource)
            .collect(Collectors.toList());
        resourceList.add(resourceLoader.getResource("classpath:/cas_common_messages.properties"));
        properties.setLocations(resourceList.toArray(Resource[]::new));
        properties.setSingleton(true);
        properties.setIgnoreResourceNotFound(true);
        return properties;
    }

    @RefreshScope
    @Bean
    @Autowired
    public HierarchicalMessageSource messageSource(@Qualifier("casCommonMessages") final Properties casCommonMessages) {
        val bean = new CasReloadableMessageBundle();
        val mb = casProperties.getMessageBundle();
        bean.setDefaultEncoding(mb.getEncoding());
        bean.setCacheSeconds(mb.getCacheSeconds());
        bean.setFallbackToSystemLocale(mb.isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(mb.isUseCodeMessage());
        bean.setBasenames(mb.getBaseNames().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        bean.setCommonMessages(casCommonMessages);
        return bean;
    }

    @Autowired
    @Bean
    public ArgumentExtractor argumentExtractor(final List<ServiceFactoryConfigurer> configurers) {
        val serviceFactoryList = new ArrayList<ServiceFactory<? extends WebApplicationService>>();
        configurers.forEach(c -> serviceFactoryList.addAll(c.buildServiceFactories()));
        AnnotationAwareOrderComparator.sortIfNecessary(configurers);
        return new DefaultArgumentExtractor(serviceFactoryList);
    }

    @Bean
    @RefreshScope
    public FactoryBean<UrlValidator> urlValidator() {
        val httpClient = this.casProperties.getHttpClient();
        val allowLocalLogoutUrls = httpClient.isAllowLocalLogoutUrls();
        val authorityValidationRegEx = httpClient.getAuthorityValidationRegEx();
        val authorityValidationRegExCaseSensitive = httpClient.isAuthorityValidationRegExCaseSensitive();
        return new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx, authorityValidationRegExCaseSensitive);
    }
}
