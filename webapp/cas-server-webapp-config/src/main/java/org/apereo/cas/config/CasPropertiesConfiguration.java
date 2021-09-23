package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CasVersion;

import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * This is {@link CasPropertiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casPropertiesConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPropertiesConfiguration {
    @Bean
    @Autowired
    public InitializingBean casPropertiesInitializingBean(final ConfigurableEnvironment environment) {
        return () -> {
            val sysProps = System.getProperties();
            val properties = new Properties();
            if (CasVersion.getVersion() != null) {
                properties.put("info.cas.version", CasVersion.getVersion());
            }
            properties.put("info.cas.date", CasVersion.getDateTime());
            properties.put("info.cas.java.home", sysProps.get("java.home"));
            properties.put("info.cas.java.vendor", sysProps.get("java.vendor"));
            properties.put("info.cas.java.version", sysProps.get("java.version"));
            val src = new PropertiesPropertySource(CasVersion.class.getName(), properties);
            environment.getPropertySources().addFirst(src);
        };
    }
}
