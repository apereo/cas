package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mail.AmazonSimpleEmailServiceEmailSender;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * This is {@link CasAmazonSimpleEmailServiceAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "ses")
@AutoConfiguration
public class CasAmazonSimpleEmailServiceAutoConfiguration {

    @ConditionalOnMissingBean(name = "amazonSimpleEmailServiceClient")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SesClient amazonSimpleEmailServiceClient(
        final CasConfigurationProperties casProperties) {
        val props = casProperties.getEmailProvider().getSes();
        val provider = ChainingAWSCredentialsProvider.getInstance(props.getCredentialAccessKey(), props.getCredentialSecretKey());
        val builder = SesClient.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, provider, props);
        return builder.build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amazonSimpleEmailServiceEmailSender")
    public EmailSender emailSender(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonSimpleEmailServiceClient") final SesClient amazonSimpleEmailServiceClient,
        @Qualifier("messageSource") final MessageSource messageSource) {
        return new AmazonSimpleEmailServiceEmailSender(amazonSimpleEmailServiceClient, messageSource, casProperties);
    }

}
