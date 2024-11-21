package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.nativex.CasNativeInfoContributor;
import lombok.val;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorFallback;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * This is {@link CasNativeSupportAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@AutoConfigureOrder(Integer.MIN_VALUE)
@AutoConfigureBefore(PropertyPlaceholderAutoConfiguration.class)
@Import({
    CasCoreBaseEnvironmentConfiguration.class,
    CasCoreBaseStandaloneConfiguration.class
})
public class CasNativeSupportAutoConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        val environment = applicationContext.getEnvironment();
        val nativeSources = CasConfigurationPropertiesEnvironmentManager.configureEnvironmentPropertySources(environment);
        val locator = applicationContext.getBean(CasConfigurationPropertiesSourceLocator.BOOTSTRAP_PROPERTY_LOCATOR_BEAN_NAME, PropertySourceLocator.class);
        nativeSources.addPropertySource(locator.locate(environment));
        environment.getPropertySources().addFirst(nativeSources);
        val cfg = new PropertySourcesPlaceholderConfigurer();
        cfg.setIgnoreUnresolvablePlaceholders(true);
        cfg.setLocalOverride(true);
        cfg.setEnvironment(environment);
        cfg.setPropertySources(environment.getPropertySources());
        return cfg;
    }

    @Bean
    @ConditionalOnEnabledInfoContributor(value = "native", fallback = InfoContributorFallback.DISABLE)
    public InfoContributor casNativeInfoContributor() {
        return new CasNativeInfoContributor();
    }
}
