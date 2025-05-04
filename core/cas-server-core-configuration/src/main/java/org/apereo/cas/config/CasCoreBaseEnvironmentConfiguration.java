package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CommaSeparatedStringToThrowablesConverter;
import org.apereo.cas.configuration.StandaloneConfigurationFilePropertiesSourceLocator;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * This is {@link CasCoreBaseEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CasCoreBaseEnvironmentConfiguration", proxyBeanMethods = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
class CasCoreBaseEnvironmentConfiguration {

    @Configuration(value = "CasCoreEnvironmentManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreEnvironmentManagerConfiguration {
        @ConditionalOnMissingBean(name = CasConfigurationPropertiesEnvironmentManager.BEAN_NAME)
        @Bean
        public static CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager(
            final ConfigurationPropertiesBindingPostProcessor binder) {
            return new CasConfigurationPropertiesEnvironmentManager(binder);
        }
    }

    @Configuration(value = "CasCoreEnvironmentFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreEnvironmentFactoryConfiguration {
        @ConfigurationPropertiesBinding
        @Bean
        public Converter<String, List<Class<? extends Throwable>>> commaSeparatedStringToThrowablesCollection() {
            return new CommaSeparatedStringToThrowablesConverter();
        }

        @ConditionalOnMissingBean(name = CipherExecutor.BEAN_NAME_CAS_CONFIGURATION_CIPHER_EXECUTOR)
        @Bean
        public static CipherExecutor<String, String> casConfigurationCipherExecutor(
            final Environment environment) {
            return new CasConfigurationJasyptCipherExecutor(environment);
        }
    }

    @Configuration(value = "CasCoreEnvironmentLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreEnvironmentLocatorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "standaloneConfigurationFilePropertiesSourceLocator")
        public static CasConfigurationPropertiesSourceLocator standaloneConfigurationFilePropertiesSourceLocator(
            @Qualifier(CipherExecutor.BEAN_NAME_CAS_CONFIGURATION_CIPHER_EXECUTOR)
            final CipherExecutor<String, String> casConfigurationCipherExecutor) {
            return new StandaloneConfigurationFilePropertiesSourceLocator(casConfigurationCipherExecutor);
        }
    }
}
