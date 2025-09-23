package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.DefaultCommunicationsManager;
import org.apereo.cas.notifications.call.PhoneCallOperator;
import org.apereo.cas.notifications.mail.DefaultEmailSender;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.notifications.mail.EmailSenderCustomizer;
import org.apereo.cas.notifications.push.DefaultNotificationSender;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;
import org.apereo.cas.notifications.sms.GroovySmsSender;
import org.apereo.cas.notifications.sms.RestfulSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreNotificationsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableScheduling
@Slf4j
@EnableConfigurationProperties({CasConfigurationProperties.class, MailProperties.class})
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications)
@AutoConfiguration
public class CasCoreNotificationsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = CommunicationsManager.BEAN_NAME)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CommunicationsManager communicationsManager(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        @Qualifier(SmsSender.BEAN_NAME)
        final SmsSender smsSender,
        @Qualifier(EmailSender.BEAN_NAME)
        final EmailSender emailSender,
        @Qualifier(PhoneCallOperator.BEAN_NAME)
        final PhoneCallOperator phoneCallOperator,
        @Qualifier(NotificationSender.BEAN_NAME)
        final NotificationSender notificationSender) {
        return new DefaultCommunicationsManager(smsSender, emailSender,
            notificationSender, phoneCallOperator, tenantExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(name = EmailSender.BEAN_NAME)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public EmailSender emailSender(
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<List<EmailSenderCustomizer>> customizers,
        final MailProperties mailProperties,
        final ObjectProvider<SslBundles> sslBundles,
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        @Qualifier("messageSource")
        final MessageSource messageSource) {
        val emailSenderCustomizers = Optional.ofNullable(customizers.getIfAvailable())
            .orElseGet(ArrayList::new)
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .toList();
        return new DefaultEmailSender(messageSource, applicationContext,
            emailSenderCustomizers, mailProperties, sslBundles, tenantExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(name = PhoneCallOperator.BEAN_NAME)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PhoneCallOperator phoneCallOperator(final CasConfigurationProperties casProperties) {
        return PhoneCallOperator.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = SmsSender.BEAN_NAME)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SmsSender smsSender(
        @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT_TRUST_STORE)
        final HttpClient supportsTrustStoreSslSocketFactoryHttpClient,
        final CasConfigurationProperties casProperties) {
        val groovy = casProperties.getSmsProvider().getGroovy();
        if (groovy.getLocation() != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
            return new GroovySmsSender(groovy.getLocation());
        }
        val rest = casProperties.getSmsProvider().getRest();
        if (StringUtils.isNotBlank(rest.getUrl())) {
            return new RestfulSmsSender(rest, supportsTrustStoreSslSocketFactoryHttpClient);
        }
        return SmsSender.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = NotificationSender.BEAN_NAME)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public NotificationSender notificationSender(
        final ObjectProvider<List<NotificationSenderExecutionPlanConfigurer>> configurerProviders) {
        val configurers = Optional.ofNullable(configurerProviders.getIfAvailable()).orElseGet(ArrayList::new);
        val results = configurers
            .stream()
            .map(cfg -> {
                LOGGER.trace("Configuring notification sender [{}]", cfg.getName());
                return cfg.configureNotificationSender();
            })
            .sorted(Comparator.comparing(NotificationSender::getOrder))
            .collect(Collectors.toList());
        return new DefaultNotificationSender(results);
    }
}
