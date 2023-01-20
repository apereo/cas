package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CommaSeparatedStringToThrowablesConverter;
import org.apereo.cas.configuration.StandaloneConfigurationFilePropertiesSourceLocator;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * This is {@link CasCoreEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
public class CasCoreEnvironmentConfiguration {

    @Configuration(value = "CasCoreEnvironmentManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    public static class CasCoreEnvironmentManagerConfiguration {
        @ConditionalOnMissingBean(name = CasConfigurationPropertiesEnvironmentManager.BEAN_NAME)
        @Bean
        public CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager(
            final ConfigurationPropertiesBindingPostProcessor binder) {
            return new CasConfigurationPropertiesEnvironmentManager(binder);
        }
    }

    @Configuration(value = "CasCoreEnvironmentFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    public static class CasCoreEnvironmentFactoryConfiguration {
        @ConfigurationPropertiesBinding
        @Bean
        public Converter<String, List<Class<? extends Throwable>>> commaSeparatedStringToThrowablesCollection() {
            return new CommaSeparatedStringToThrowablesConverter();
        }

        @ConditionalOnMissingBean(name = "casConfigurationCipherExecutor")
        @Bean
        public CipherExecutor<String, String> casConfigurationCipherExecutor(
            final Environment environment) {
            return new CasConfigurationJasyptCipherExecutor(environment);
        }

        @ConditionalOnMissingBean(name = "configurationPropertiesLoaderFactory")
        @Bean
        public ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory(
            @Qualifier("casConfigurationCipherExecutor") final CipherExecutor<String, String> casConfigurationCipherExecutor,
            final Environment environment) {
            return new ConfigurationPropertiesLoaderFactory(casConfigurationCipherExecutor, environment);
        }
    }

    @Configuration(value = "CasCoreEnvironmentLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    public static class CasCoreEnvironmentLocatorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "standaloneConfigurationFilePropertiesSourceLocator")
        public CasConfigurationPropertiesSourceLocator standaloneConfigurationFilePropertiesSourceLocator(
            @Qualifier(CasConfigurationPropertiesEnvironmentManager.BEAN_NAME) final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager,
            @Qualifier("configurationPropertiesLoaderFactory") final ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory) {
            return new StandaloneConfigurationFilePropertiesSourceLocator(
                configurationPropertiesEnvironmentManager, configurationPropertiesLoaderFactory);
        }
    }
}
