package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.DefaultCasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.DockerSecretsPropertySourceLocator;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.ResourceLoader;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasCoreBaseStandaloneConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CasCoreBaseStandaloneConfiguration", proxyBeanMethods = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
class CasCoreBaseStandaloneConfiguration {

    @Configuration(value = "CasCoreBootstrapStandaloneSourcesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreBootstrapStandaloneSourcesConfiguration implements PriorityOrdered {

        @Bean
        @ConditionalOnMissingBean(name = "casDockerSecretsPropertySourceLocator")
        public CasConfigurationPropertiesSourceLocator casDockerSecretsPropertySourceLocator() {
            return new DockerSecretsPropertySourceLocator();
        }
        
        @Bean
        public static PropertySourceLocator casCoreBootstrapPropertySourceLocator(
            final List<CasConfigurationPropertiesSourceLocator> locatorList,
            final ResourceLoader resourceLoader) {
            AnnotationAwareOrderComparator.sortIfNecessary(locatorList);
            return environment -> {
                val composite = new CompositePropertySource(CasConfigurationPropertiesSourceLocator.BOOTSTRAP_PROPERTY_LOCATOR_BEAN_NAME);
                locatorList
                    .stream()
                    .map(locator -> locator.locate(environment, resourceLoader))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(composite::addPropertySource);
                return composite;
            };
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    @Configuration(value = "CasCoreBootstrapStandaloneLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Profile({
        CasConfigurationPropertiesSourceLocator.PROFILE_STANDALONE,
        CasConfigurationPropertiesSourceLocator.PROFILE_NATIVE,
        CasConfigurationPropertiesSourceLocator.PROFILE_EMBEDDED
    })
    @Lazy(false)
    static class CasCoreBootstrapStandaloneLocatorConfiguration {
        @ConditionalOnMissingBean(name = "casConfigurationPropertiesSourceLocator")
        @Bean
        public static CasConfigurationPropertiesSourceLocator casConfigurationPropertiesSourceLocator(
            @Qualifier(CipherExecutor.BEAN_NAME_CAS_CONFIGURATION_CIPHER_EXECUTOR)
            final CipherExecutor<String, String> casConfigurationCipherExecutor) {
            return new DefaultCasConfigurationPropertiesSourceLocator(casConfigurationCipherExecutor);
        }
    }
}
