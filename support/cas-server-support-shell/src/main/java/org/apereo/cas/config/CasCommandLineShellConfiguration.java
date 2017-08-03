package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCommandLineShellConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casCommandLineShellConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCommandLineShellConfiguration {
}
