package org.apereo.cas.config;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * This is {@link CasNativeWebConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@Import({
    CasCoreBaseStandaloneConfiguration.class,
    CasCoreBaseEnvironmentConfiguration.class
})
public class CasNativeWebConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casCoreBootstrapPropertySourceLocator")
        final PropertySourceLocator casCoreBootstrapPropertySourceLocator) {
        val environment = applicationContext.getEnvironment();

        val nativeSources = buildNativePropertySources(casCoreBootstrapPropertySourceLocator, environment);
        environment.getPropertySources().addFirst(nativeSources);

        val cfg = new PropertySourcesPlaceholderConfigurer();
        cfg.setIgnoreUnresolvablePlaceholders(true);
        cfg.setLocalOverride(true);
        cfg.setEnvironment(environment);
        cfg.setPropertySources(environment.getPropertySources());
        return cfg;
    }

    private static CompositePropertySource buildNativePropertySources(final PropertySourceLocator casCoreBootstrapPropertySourceLocator,
                                                                      final ConfigurableEnvironment environment) {
        val nativePropertySources = new CompositePropertySource("casNativeCompositeSource");
        FunctionUtils.doIfNotNull(environment.getPropertySources().get("commandLineArgs"), nativePropertySources::addFirstPropertySource);
        FunctionUtils.doIfNotNull(environment.getPropertySources().get("systemProperties"), nativePropertySources::addPropertySource);
        FunctionUtils.doIfNotNull(environment.getPropertySources().get("systemEnvironment"), nativePropertySources::addPropertySource);
        nativePropertySources.addPropertySource(casCoreBootstrapPropertySourceLocator.locate(environment));
        return nativePropertySources;
    }

}
