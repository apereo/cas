package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logging.CloudWatchLogsEndpoint;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

/**
 * This is {@link CasAmazonCloudWatchLoggingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Logging, module = "cloudwatch")
@Configuration(value = "CasAmazonCloudWatchLoggingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasAmazonCloudWatchLoggingConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "cloudWatchLogsClient")
    @RefreshScope
    public CloudWatchLogsClient cloudWatchLogsClient(final CasConfigurationProperties casProperties) {
        val amz = casProperties.getLogging().getCloudwatch();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());
        val builder = CloudWatchLogsClient.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, credentials, amz);
        return builder.build();
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public CloudWatchLogsEndpoint cloudWatchLogsEndpoint(final CasConfigurationProperties casProperties,
                                                         final ConfigurableApplicationContext applicationContext,
                                                         @Qualifier("cloudWatchLogsClient")
                                                         final ObjectProvider<@NonNull CloudWatchLogsClient> cloudWatchLogsClient) {
        val cloudwatch = casProperties.getLogging().getCloudwatch();
        Assert.hasText(cloudwatch.getLogGroupName(), "CloudWatch log group name cannot be blank");
        Assert.hasText(cloudwatch.getLogStreamName(), "CloudWatch log stream name cannot be blank");
        return new CloudWatchLogsEndpoint(casProperties, applicationContext, cloudWatchLogsClient);
    }
}
