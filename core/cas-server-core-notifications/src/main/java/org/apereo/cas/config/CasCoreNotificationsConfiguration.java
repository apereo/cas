package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.push.DefaultNotificationSender;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;
import org.apereo.cas.notifications.sms.GroovySmsSender;
import org.apereo.cas.notifications.sms.RestfulSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreNotificationsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreNotificationsConfiguration", proxyBeanMethods = false)
@EnableScheduling
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreNotificationsConfiguration {
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "communicationsManager")
    public CommunicationsManager communicationsManager(
        @Qualifier("mailSender")
        final ObjectProvider<JavaMailSender> mailSender,
        @Qualifier("smsSender")
        final SmsSender smsSender,
        @Qualifier("notificationSender")
        final NotificationSender notificationSender) {
        return new CommunicationsManager(smsSender, mailSender.getIfAvailable(), notificationSender);
    }

    @Bean
    @ConditionalOnMissingBean(name = "smsSender")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val groovy = casProperties.getSmsProvider().getGroovy();
        if (groovy.getLocation() != null) {
            return new GroovySmsSender(groovy.getLocation());
        }
        val rest = casProperties.getSmsProvider().getRest();
        if (StringUtils.isNotBlank(rest.getUrl())) {
            return new RestfulSmsSender(rest);
        }
        return SmsSender.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = "notificationSender")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public NotificationSender notificationSender(
        final ObjectProvider<List<NotificationSenderExecutionPlanConfigurer>> configurerProviders) {
        val configurers = Optional.ofNullable(configurerProviders.getIfAvailable()).orElse(new ArrayList<>());
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
