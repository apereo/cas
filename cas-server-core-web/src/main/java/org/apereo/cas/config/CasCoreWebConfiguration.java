package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.ClearpassApplicationContextWrapper;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.List;

/**
 * This is {@link CasCoreWebConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebConfiguration")
public class CasCoreWebConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceFactoryList")
    private List serviceFactoryList;

    @Bean
    @RefreshScope
    public BaseApplicationContextWrapper clearpassApplicationContextWrapper() {
        final ClearpassApplicationContextWrapper w =
                new ClearpassApplicationContextWrapper();
        w.setCacheCredential(casProperties.getClearpass().isCacheCredential());
        return w;
    }

    @Bean
    public ArgumentExtractor defaultArgumentExtractor() {
        final DefaultArgumentExtractor a = new DefaultArgumentExtractor(serviceFactoryList);
        return a;
    }

    @RefreshScope
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        final CasReloadableMessageBundle bean = new CasReloadableMessageBundle();
        bean.setDefaultEncoding(casProperties.getMessageBundle().getEncoding());
        bean.setCacheSeconds(casProperties.getMessageBundle().getCacheSeconds());
        bean.setFallbackToSystemLocale(casProperties.getMessageBundle().isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(casProperties.getMessageBundle().isUseCodeMessage());
        bean.setBasenames(casProperties.getMessageBundle().getBaseNames());
        return bean;
    }
}
