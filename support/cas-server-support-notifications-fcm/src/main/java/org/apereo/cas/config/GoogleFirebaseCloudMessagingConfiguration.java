package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.GoogleFirebaseCloudMessagingNotificationSender;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

/**
 * This is {@link GoogleFirebaseCloudMessagingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "googleFirebaseCloudMessagingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleFirebaseCloudMessagingConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;


    @Bean
    @ConditionalOnMissingBean(name = "firebaseCloudMessagingNotificationSenderExecutionPlanConfigurer")
    @RefreshScope
    @Autowired
    public NotificationSenderExecutionPlanConfigurer firebaseCloudMessagingNotificationSenderExecutionPlanConfigurer(
        @Qualifier("firebaseCloudMessagingNotificationSender") final NotificationSender firebaseCloudMessagingNotificationSender) {
        return () -> firebaseCloudMessagingNotificationSender;
    }

    @SneakyThrows
    private GoogleCredentials getCredentials() {
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (final Exception e) {
            val firebase = casProperties.getGoogleFirebaseMessaging();
            val keyPath = firebase.getServiceAccountKey().getLocation().getFile().getCanonicalPath();
            return GoogleCredentials
                .fromStream(new FileInputStream(keyPath))
                .createScoped(firebase.getScopes());
        }
    }

    @Configuration(value = "GoogleFirebaseCloudMessagingInternalConfiguration", proxyBeanMethods = false)
    public class GoogleFirebaseCloudMessagingInternalConfiguration {
        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "firebaseCloudMessagingNotificationSender")
        public NotificationSender firebaseCloudMessagingNotificationSender() {
            val firebase = casProperties.getGoogleFirebaseMessaging();
            val options = new FirebaseOptions.Builder()
                .setCredentials(getCredentials())
                .setDatabaseUrl(firebase.getDatabaseUrl())
                .build();
            FirebaseApp.initializeApp(options);
            return new GoogleFirebaseCloudMessagingNotificationSender(firebase);
        }
    }
}
