package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.APNMessagingNotificationSender;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasAPNMessagingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "apn")
@AutoConfiguration
public class CasAPNMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "apnNotificationSenderExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public NotificationSenderExecutionPlanConfigurer apnNotificationSenderExecutionPlanConfigurer(
        @Qualifier("apnMessagingNotificationSender")
        final NotificationSender apnMessagingNotificationSender) {
        return () -> apnMessagingNotificationSender;
    }

    @Bean
    @ConditionalOnMissingBean(name = "apnsClient")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ApnsClient apnsClient(final CasConfigurationProperties casProperties) throws Exception {
        val apnMessaging = casProperties.getApnMessaging();
        try (val p8File = apnMessaging.getAuthenticationKey().getLocation().getInputStream()) {
            return new ApnsClientBuilder()
                .setApnsServer(Strings.CI.equals(apnMessaging.getServer(), "production")
                    ? ApnsClientBuilder.PRODUCTION_APNS_HOST
                    : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setSigningKey(ApnsSigningKey.loadFromInputStream(p8File, apnMessaging.getTeamId(), apnMessaging.getKeyId()))
                .build();
        }
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "apnMessagingNotificationSender")
    public NotificationSender apnMessagingNotificationSender(
        final CasConfigurationProperties casProperties,
        @Qualifier("apnsClient")
        final ApnsClient apnsClient) {
        return new APNMessagingNotificationSender(apnsClient, casProperties.getApnMessaging());
    }
}
