package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.AmazonSimpleNotificationServiceSmsSender;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * This is {@link AmazonSimpleNotificationServiceSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "amazonSimpleNotificationServiceSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AmazonSimpleNotificationServiceSmsConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val sns = casProperties.getSmsProvider().getSns();
        val clientBuilder = SnsClient.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(clientBuilder,
            ChainingAWSCredentialsProvider.getInstance(sns.getCredentialAccessKey(), sns.getCredentialSecretKey(), sns.getProfilePath(), sns.getProfileName()), sns);
        return new AmazonSimpleNotificationServiceSmsSender(clientBuilder.build(), sns);
    }
}
