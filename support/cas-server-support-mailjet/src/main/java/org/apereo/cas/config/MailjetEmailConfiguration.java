package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mail.MailjetEmailSender;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link MailjetEmailConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "mailjet-email")
@Configuration(value = "MailjetEmailConfiguration", proxyBeanMethods = false)
class MailjetEmailConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mailjetEmailClient")
    @Bean
    public MailjetClient mailjetEmailClient(final CasConfigurationProperties casProperties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val options = ClientOptions.builder()
            .apiKey(resolver.resolve(casProperties.getEmailProvider().getMailjet().getApiKey()))
            .apiSecretKey(resolver.resolve(casProperties.getEmailProvider().getMailjet().getSecretKey()))
            .bearerAccessToken(resolver.resolve(casProperties.getEmailProvider().getMailjet().getBearerAccessToken()))
            .build();
        return new MailjetClient(options);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mailjetEmailSender")
    public EmailSender emailSender(
        @Qualifier("mailjetEmailClient") final MailjetClient mailjetEmailClient,
        final CasConfigurationProperties casProperties,
        @Qualifier("messageSource") final MessageSource messageSource) {
        return new MailjetEmailSender(mailjetEmailClient, messageSource, casProperties);
    }
}
