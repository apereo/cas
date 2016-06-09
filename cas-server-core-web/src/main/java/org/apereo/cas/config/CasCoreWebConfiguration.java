package org.apereo.cas.config;

import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.configuration.model.support.clearpass.ClearpassProperties;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.ClearpassApplicationContextWrapper;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(MessageBundleProperties.class)
public class CasCoreWebConfiguration {

    @Autowired
    private ClearpassProperties properties;

    @Autowired
    private MessageBundleProperties messageBundleProperties;

    @Bean
    @RefreshScope
    public BaseApplicationContextWrapper clearpassApplicationContextWrapper() {
        final ClearpassApplicationContextWrapper w =
                new ClearpassApplicationContextWrapper();
        w.setCacheCredential(properties.isCacheCredential());
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
        bean.setDefaultEncoding(this.messageBundleProperties.getEncoding());
        bean.setCacheSeconds(this.messageBundleProperties.getCacheSeconds());
        bean.setFallbackToSystemLocale(this.messageBundleProperties.isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(this.messageBundleProperties.isUseCodeMessage());
        bean.setBasenames(this.messageBundleProperties.getBaseNames());
        return bean;
    }
}
