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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreNotificationsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreNotificationsConfiguration")
@EnableScheduling
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreNotificationsConfiguration {
    @Autowired
    @Qualifier("mailSender")
    private ObjectProvider<JavaMailSender> mailSender;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(name = "communicationsManager")
    public CommunicationsManager communicationsManager() {
        return new CommunicationsManager(smsSender(), mailSender.getIfAvailable(), notificationSender());
    }

    @Bean
    @ConditionalOnMissingBean(name = "smsSender")
    @RefreshScope
    public SmsSender smsSender() {
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
    @RefreshScope
    public NotificationSender notificationSender() {
        val configurers = applicationContext.getBeansOfType(NotificationSenderExecutionPlanConfigurer.class, false, true);
        val results = configurers.values()
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
