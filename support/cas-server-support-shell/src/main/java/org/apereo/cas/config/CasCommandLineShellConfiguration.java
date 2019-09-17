package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

/**
 * This is {@link CasCommandLineShellConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casCommandLineShellConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCommandLineShellConfiguration {

    @Bean
    public PromptProvider shellPromptProvider() {
        return () -> new AttributedString("cas>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
