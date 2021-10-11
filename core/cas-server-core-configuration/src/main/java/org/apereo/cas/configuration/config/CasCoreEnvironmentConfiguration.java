package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CommaSeparatedStringToThrowablesConverter;
import org.apereo.cas.configuration.StandaloneConfigurationFilePropertiesSourceLocator;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * This is {@link CasCoreEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CasCoreEnvironmentConfiguration", proxyBeanMethods = false)
public class CasCoreEnvironmentConfiguration {

    @Configuration(value = "CasCoreEnvironmentManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEnvironmentManagerConfiguration {
        @Autowired
        @ConditionalOnMissingBean(name = "configurationPropertiesEnvironmentManager")
        @Bean
        public CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager(
            final ConfigurationPropertiesBindingPostProcessor binder,
            final Environment environment) {
            return new CasConfigurationPropertiesEnvironmentManager(binder, environment);
        }
    }

    @Configuration(value = "CasCoreEnvironmentFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEnvironmentFactoryConfiguration {
        @ConfigurationPropertiesBinding
        @Bean
        public Converter<String, List<Class<? extends Throwable>>> commaSeparatedStringToThrowablesCollection() {
            return new CommaSeparatedStringToThrowablesConverter();
        }

        @ConditionalOnMissingBean(name = "casConfigurationCipherExecutor")
        @Bean
        @Autowired
        public CipherExecutor<String, String> casConfigurationCipherExecutor(
            final Environment environment) {
            return new CasConfigurationJasyptCipherExecutor(environment);
        }

        @ConditionalOnMissingBean(name = "configurationPropertiesLoaderFactory")
        @Bean
        @Autowired
        public ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory(
            @Qualifier("casConfigurationCipherExecutor")
            final CipherExecutor<String, String> casConfigurationCipherExecutor,
            final Environment environment) {
            return new ConfigurationPropertiesLoaderFactory(casConfigurationCipherExecutor, environment);
        }
    }

    @Configuration(value = "CasCoreEnvironmentLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEnvironmentLocatorConfiguration {
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "standaloneConfigurationFilePropertiesSourceLocator")
        public CasConfigurationPropertiesSourceLocator standaloneConfigurationFilePropertiesSourceLocator(
            @Qualifier("configurationPropertiesEnvironmentManager")
            final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager,
            @Qualifier("configurationPropertiesLoaderFactory")
            final ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory) {
            return new StandaloneConfigurationFilePropertiesSourceLocator(
                configurationPropertiesEnvironmentManager, configurationPropertiesLoaderFactory);
        }
    }
}
