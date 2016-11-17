package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CasVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * This is {@link CasPropertiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casPropertiesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPropertiesConfiguration {
    @Autowired
    private ConfigurableEnvironment environment;

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        final Properties sysProps = System.getProperties();
        final Properties properties = new Properties();
        if (CasVersion.getVersion() != null) {
            properties.put("info.cas.version", CasVersion.getVersion());
        }
        properties.put("info.cas.date", CasVersion.getDateTime());
        properties.put("info.cas.java.home", sysProps.get("java.home"));
        properties.put("info.cas.java.vendor", sysProps.get("java.vendor"));
        properties.put("info.cas.java.version", sysProps.get("java.version"));
        final PropertiesPropertySource src = new PropertiesPropertySource(CasVersion.class.getName(), properties);
        this.environment.getPropertySources().addFirst(src);
    }
}
