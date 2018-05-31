package org.apereo.cas.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.AmazonSimpleNotificationServiceSmsSender;
import org.apereo.cas.util.io.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link AmazonSimpleNotificationServiceSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("amazonSimpleNotificationServiceSmsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AmazonSimpleNotificationServiceSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SmsSender smsSender() {
        final var sns = casProperties.getSmsProvider().getSns();
        final var endpoint = new AwsClientBuilder.EndpointConfiguration(sns.getEndpoint(), sns.getRegion());
        final var snsClient = AmazonSNSClient.builder()
            .withCredentials(ChainingAWSCredentialsProvider.getInstance(sns.getCredentialAccessKey(),
                sns.getCredentialSecretKey(), sns.getCredentialsPropertiesFile(),
                sns.getProfilePath(), sns.getProfileName()))
            .withRegion(sns.getRegion())
            .withEndpointConfiguration(endpoint)
            .build();

        return new AmazonSimpleNotificationServiceSmsSender(snsClient, sns);
    }
}
