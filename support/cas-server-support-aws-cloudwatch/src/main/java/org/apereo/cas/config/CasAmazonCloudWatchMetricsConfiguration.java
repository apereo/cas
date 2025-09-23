package org.apereo.cas.config;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link CasAmazonCloudWatchMetricsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "aws")
@ConditionalOnEnabledMetricsExport("cloudwatch")
@Configuration(value = "CasAmazonCloudWatchMetricsConfiguration", proxyBeanMethods = false)
class CasAmazonCloudWatchMetricsConfiguration {
    /**
     * System property to specify the endpoint for AWS cloudwatch.
     */
    public static final String AWS_SYSTEM_PROPERTY_CLOUDWATCH_ENDPOINT = "aws.cloudwatch.endpoint";

    private static final int AWS_CLOUDWATCH_DEFAULT_STEP_SECONDS = 30;

    private static final Map<String, String> CLOUDWATCH_CONFIGURATION = Map.of(
        "cloudwatch.numThreads", "2",
        "cloudwatch.connectTimeout", Duration.ofSeconds(5).toString(),
        "cloudwatch.readTimeout", Duration.ofSeconds(5).toString(),
        "cloudwatch.batchSize", "10",
        "cloudwatch.namespace", "apereo-cas",
        "cloudwatch.step", Duration.ofSeconds(AWS_CLOUDWATCH_DEFAULT_STEP_SECONDS).toString());

    @Bean
    @ConditionalOnMissingBean(name = "cloudWatchClient")
    public CloudWatchAsyncClient cloudWatchClient() throws Exception {
        val clientBuilder = CloudWatchAsyncClient
            .builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(ChainingAWSCredentialsProvider.getInstance());

        val environmentVariable = AWS_SYSTEM_PROPERTY_CLOUDWATCH_ENDPOINT.toUpperCase(Locale.ENGLISH).replace('.', '_');
        val endpoint = System.getProperty(AWS_SYSTEM_PROPERTY_CLOUDWATCH_ENDPOINT, System.getenv(environmentVariable));
        if (StringUtils.isNotBlank(endpoint)) {
            clientBuilder.endpointOverride(new URI(endpoint));
        }
        return clientBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "cloudWatchMeterRegistry")
    public MeterRegistry cloudWatchMeterRegistry(
        final Clock clock,
        @Qualifier("cloudWatchClient") final CloudWatchAsyncClient cloudWatchClient) {
        val cloudWatchConfig = new CloudWatchConfig() {
            @Override
            public String get(final String key) {
                return CLOUDWATCH_CONFIGURATION.get(key);
            }
        };

        return new CloudWatchMeterRegistry(cloudWatchConfig, clock,
            cloudWatchClient, Thread.ofVirtual().factory());
    }
}
