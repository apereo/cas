package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.AmazonSimpleNotificationServiceSmsSender;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * This is {@link CasAmazonSimpleNotificationServiceSmsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "aws-sns")
@AutoConfiguration
public class CasAmazonSimpleNotificationServiceSmsAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val sns = casProperties.getSmsProvider().getSns();
        val clientBuilder = SnsClient.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(clientBuilder,
            ChainingAWSCredentialsProvider.getInstance(sns.getCredentialAccessKey(), sns.getCredentialSecretKey(), sns.getProfilePath(), sns.getProfileName()), sns);
        return new AmazonSimpleNotificationServiceSmsSender(clientBuilder.build(), sns);
    }
}
