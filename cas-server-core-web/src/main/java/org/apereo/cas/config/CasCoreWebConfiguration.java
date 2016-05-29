package org.apereo.cas.config;

import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.ClearpassApplicationContextWrapper;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
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
    
    @Bean
    @RefreshScope
    public BaseApplicationContextWrapper clearpassApplicationContextWrapper() {
        return new ClearpassApplicationContextWrapper();
    }
    
    @Bean
    public ArgumentExtractor defaultArgumentExtractor() {
        return new DefaultArgumentExtractor();
    }
    
    @RefreshScope
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        return new CasReloadableMessageBundle();
    }
}
