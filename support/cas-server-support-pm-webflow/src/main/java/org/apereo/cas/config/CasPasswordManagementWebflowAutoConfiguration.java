package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasPasswordManagementWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@AutoConfiguration
@Import({
    PasswordManagementWebflowConfiguration.class,
    PasswordManagementForgotUsernameConfiguration.class
})
public class CasPasswordManagementWebflowAutoConfiguration {
}
