package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.SlackNotificationSender;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSlackMessagingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "slack")
@AutoConfiguration
public class CasSlackMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "slackNotificationSenderExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public NotificationSenderExecutionPlanConfigurer slackNotificationSenderExecutionPlanConfigurer(
        @Qualifier("slackNotificationSender") final NotificationSender slackNotificationSender) {
        return () -> slackNotificationSender;
    }

    @Configuration(value = "SlackMessagingInternalConfiguration", proxyBeanMethods = false)
    static class SlackMessagingInternalConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.slack-messaging.api-token");

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "slackNotificationSender")
        public NotificationSender slackNotificationSender(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(NotificationSender.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new SlackNotificationSender(casProperties.getSlackMessaging()))
                .otherwise(NotificationSender::noOp)
                .get();
        }
    }
}
