package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logging.GoogleCloudLoggingWebInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Nonnull;

/**
 * This is {@link GoogleCloudLoggingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Logging, module = "gcp")
public class GoogleCloudLoggingConfiguration {
    @Bean
    public WebMvcConfigurer googleCloudLoggingWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(
                @Nonnull
                final InterceptorRegistry registry) {
                registry.addWebRequestInterceptor(new GoogleCloudLoggingWebInterceptor()).addPathPatterns("/**");
            }
        };
    }
}
