package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.GoogleFirebaseCloudMessagingNotificationSender;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.io.FileInputStream;

/**
 * This is {@link CasGoogleFirebaseCloudMessagingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "fcm")
@AutoConfiguration
public class CasGoogleFirebaseCloudMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "firebaseCloudMessagingNotificationSenderExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public NotificationSenderExecutionPlanConfigurer firebaseCloudMessagingNotificationSenderExecutionPlanConfigurer(
        @Qualifier("firebaseCloudMessagingNotificationSender")
        final NotificationSender firebaseCloudMessagingNotificationSender) {
        return () -> firebaseCloudMessagingNotificationSender;
    }


    @Configuration(value = "GoogleFirebaseCloudMessagingInternalConfiguration", proxyBeanMethods = false)
    static class GoogleFirebaseCloudMessagingInternalConfiguration {

        private static GoogleCredentials getCredentials(
            final CasConfigurationProperties casProperties) throws Exception {
            val firebase = casProperties.getGoogleFirebaseMessaging();
            if (firebase.getServiceAccountKey().getLocation() != null) {
                val keyPath = firebase.getServiceAccountKey().getLocation().getFile().getCanonicalPath();
                try (val credentialsStream = new FileInputStream(keyPath)) {
                    return GoogleCredentials.fromStream(credentialsStream).createScoped(firebase.getScopes());
                }
            }
            return GoogleCredentials.getApplicationDefault();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "firebaseCloudMessagingNotificationSender")
        public NotificationSender firebaseCloudMessagingNotificationSender(
            final CasConfigurationProperties casProperties) throws Exception {
            val firebase = casProperties.getGoogleFirebaseMessaging();
            val options = FirebaseOptions.builder()
                .setCredentials(getCredentials(casProperties))
                .setDatabaseUrl(firebase.getDatabaseUrl())
                .build();
            val firebaseApp = FirebaseApp.initializeApp(options);
            return new GoogleFirebaseCloudMessagingNotificationSender(firebase, firebaseApp);
        }
    }
}
