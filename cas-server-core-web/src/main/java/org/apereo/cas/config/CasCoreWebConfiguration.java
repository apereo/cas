package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.ClearpassApplicationContextWrapper;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

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
    
    @Bean
    @RefreshScope
    public BaseApplicationContextWrapper clearpassApplicationContextWrapper() {
        final ClearpassApplicationContextWrapper w =
                new ClearpassApplicationContextWrapper();
        w.setCacheCredential(casProperties.getClearpassProperties().isCacheCredential());
        return w;
    }

    @Bean
    public ArgumentExtractor defaultArgumentExtractor() {
        return new DefaultArgumentExtractor();
    }

    @RefreshScope
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        final CasReloadableMessageBundle bean = new CasReloadableMessageBundle();
        bean.setDefaultEncoding(casProperties.getMessageBundleProperties().getEncoding());
        bean.setCacheSeconds(casProperties.getMessageBundleProperties().getCacheSeconds());
        bean.setFallbackToSystemLocale(casProperties.getMessageBundleProperties().isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(casProperties.getMessageBundleProperties().isUseCodeMessage());
        bean.setBasenames(casProperties.getMessageBundleProperties().getBaseNames());
        return bean;
    }
}
