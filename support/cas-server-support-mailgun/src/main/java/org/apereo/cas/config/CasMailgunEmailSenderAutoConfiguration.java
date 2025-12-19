package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mail.MailgunEmailSender;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasMailgunEmailSenderAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "mailgun")
@AutoConfiguration
public class CasMailgunEmailSenderAutoConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mailgunMessagesApi")
    @Bean
    public MailgunMessagesApi mailgunMessagesApi(final CasConfigurationProperties casProperties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val mailgun = casProperties.getEmailProvider().getMailgun();
        return MailgunClient.config(
                resolver.resolve(mailgun.getBaseUrl()),
                resolver.resolve(mailgun.getApiKey()))
            .createApi(MailgunMessagesApi.class);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mailgunEmailSender")
    public EmailSender emailSender(
        @Qualifier("mailgunMessagesApi") final MailgunMessagesApi mailjetEmailClient,
        final CasConfigurationProperties casProperties,
        @Qualifier("messageSource") final MessageSource messageSource) {
        return new MailgunEmailSender(mailjetEmailClient, messageSource, casProperties);
    }
}
