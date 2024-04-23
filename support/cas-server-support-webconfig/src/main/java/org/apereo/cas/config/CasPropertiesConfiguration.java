package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * This is {@link CasPropertiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@Configuration(value = "CasPropertiesConfiguration", proxyBeanMethods = false)
@Lazy(false)
class CasPropertiesConfiguration {
    @Bean
    public InitializingBean casPropertiesInitializingBean(final ConfigurableEnvironment environment) {
        return () -> {
            val sysProps = System.getProperties();
            val properties = new Properties();
            FunctionUtils.doIfNotNull(CasVersion.getVersion(), value -> properties.put("info.cas.version", value));
            FunctionUtils.doIfNotNull(sysProps.get("java.home"), value -> properties.put("info.cas.java.home", value));
            properties.put("info.cas.java.vendor", sysProps.get("java.vendor"));
            properties.put("info.cas.java.version", sysProps.get("java.version"));
            val src = new PropertiesPropertySource(CasVersion.class.getName(), properties);
            environment.getPropertySources().addFirst(src);
        };
    }
}
