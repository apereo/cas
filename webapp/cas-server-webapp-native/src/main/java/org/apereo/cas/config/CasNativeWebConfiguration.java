package org.apereo.cas.config;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
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
@AutoConfigureOrder(Integer.MIN_VALUE)
@AutoConfigureBefore(PropertyPlaceholderAutoConfiguration.class)
@Import({
    CasCoreBaseEnvironmentConfiguration.class,
    CasCoreBaseStandaloneConfiguration.class
})
public class CasNativeWebConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        val environment = applicationContext.getEnvironment();
        val nativeSources = buildNativePropertySources(environment);

        val locator = applicationContext.getBean("casCoreBootstrapPropertySourceLocator", PropertySourceLocator.class);
        nativeSources.addPropertySource(locator.locate(environment));
        
        environment.getPropertySources().addFirst(nativeSources);
        val cfg = new PropertySourcesPlaceholderConfigurer();
        cfg.setIgnoreUnresolvablePlaceholders(true);
        cfg.setLocalOverride(true);
        cfg.setEnvironment(environment);
        cfg.setPropertySources(environment.getPropertySources());
        return cfg;
    }

    private static CompositePropertySource buildNativePropertySources(final ConfigurableEnvironment environment) {
        val nativePropertySources = new CompositePropertySource("casNativeCompositeSource");
        FunctionUtils.doIfNotNull(environment.getPropertySources().get("commandLineArgs"), nativePropertySources::addFirstPropertySource);
        FunctionUtils.doIfNotNull(environment.getPropertySources().get("systemProperties"), nativePropertySources::addPropertySource);
        FunctionUtils.doIfNotNull(environment.getPropertySources().get("systemEnvironment"), nativePropertySources::addPropertySource);
        return nativePropertySources;
    }

}
