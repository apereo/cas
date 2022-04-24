package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

/**
 * This is {@link CasCommandLineShellConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Core, module = "shell")
@AutoConfiguration
public class CasCommandLineShellConfiguration {

    @Bean
    public PromptProvider shellPromptProvider() {
        return () -> new AttributedString("cas>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
