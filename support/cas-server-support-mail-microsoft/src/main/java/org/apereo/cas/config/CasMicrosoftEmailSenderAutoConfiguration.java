package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mail.MicrosoftEmailSenderCustomizer;
import org.apereo.cas.notifications.mail.EmailSenderCustomizer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasMicrosoftEmailSenderAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "microsoft")
@AutoConfiguration
public class CasMicrosoftEmailSenderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "microsoftEmailSenderCustomizer")
    @RefreshScope
    public EmailSenderCustomizer microsoftEmailSenderCustomizer(final CasConfigurationProperties casProperties) {
        return new MicrosoftEmailSenderCustomizer(casProperties);
    }
}
