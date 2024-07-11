package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.sms.MailjetSmsSender;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link MailjetSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "mailjet-sms")
@Configuration(value = "MailjetSmsConfiguration", proxyBeanMethods = false)
class MailjetSmsConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mailjetSmsClient")
    @Bean
    public MailjetClient mailjetSmsClient(final CasConfigurationProperties casProperties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val mailjet = casProperties.getSmsProvider().getMailjet();
        val options = ClientOptions.builder()
            .apiKey(resolver.resolve(mailjet.getApiKey()))
            .apiSecretKey(resolver.resolve(mailjet.getSecretKey()))
            .bearerAccessToken(resolver.resolve(mailjet.getBearerAccessToken()))
            .build();
        return new MailjetClient(options);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mailjetSmsSender")
    public SmsSender smsSender(
        final CasConfigurationProperties casProperties,
        @Qualifier("mailjetSmsClient") final MailjetClient mailjetSmsClient) {
        val mailjet = casProperties.getSmsProvider().getMailjet();
        return new MailjetSmsSender(mailjetSmsClient, mailjet);
    }
}
