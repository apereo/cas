package org.apereo.cas.config;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.AmazonSimpleNotificationServiceSmsSender;
import org.apereo.cas.util.io.SmsSender;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
@Configuration(value = "amazonSimpleNotificationServiceSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AmazonSimpleNotificationServiceSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SmsSender smsSender() {
        val sns = casProperties.getSmsProvider().getSns();
        val clientBuilder = AmazonSNSClient.builder()
            .withCredentials(ChainingAWSCredentialsProvider.getInstance(sns.getCredentialAccessKey(),
                sns.getCredentialSecretKey(), sns.getCredentialsPropertiesFile(),
                sns.getProfilePath(), sns.getProfileName()));

        if (StringUtils.isNotBlank(sns.getEndpoint())) {
            LOGGER.trace("Setting endpoint [{}]", sns.getEndpoint());
            val endpoint = new AwsClientBuilder.EndpointConfiguration(sns.getEndpoint(), sns.getRegion());
            clientBuilder.withEndpointConfiguration(endpoint);
        }

        if (StringUtils.isNotBlank(sns.getRegion())) {
            LOGGER.trace("Setting client region [{}]", sns.getRegion());
            clientBuilder.withRegion(sns.getRegion());
        }

        return new AmazonSimpleNotificationServiceSmsSender(clientBuilder.build(), sns);
    }
}
