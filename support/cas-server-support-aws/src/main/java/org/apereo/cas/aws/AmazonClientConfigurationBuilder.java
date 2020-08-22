package org.apereo.cas.aws;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.Beans;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;

import java.net.InetAddress;
import java.net.URI;

/**
 * This is {@link AmazonClientConfigurationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonClientConfigurationBuilder {

    /**
     * Build client configuration.
     *
     * @param builder             the builder
     * @param credentialsProvider the credentials provider
     * @param props               the props
     * @return the aws sync client builder
     */
    @SneakyThrows
    public static AwsSyncClientBuilder prepareClientBuilder(final AwsSyncClientBuilder builder,
                                                            final AwsCredentialsProvider credentialsProvider,
                                                            final BaseAmazonWebServicesProperties props) {
        val proxyConfig = ProxyConfiguration.builder();
        if (StringUtils.isNotBlank(props.getProxyHost())) {
            proxyConfig.endpoint(new URI(props.getProxyHost()))
                .password(props.getProxyPassword())
                .username(props.getProxyUsername());
        }

        val httpClientBuilder = ApacheHttpClient.builder()
            .proxyConfiguration(proxyConfig.build());

        httpClientBuilder
            .useIdleConnectionReaper(props.isUseReaper())
            .socketTimeout(Beans.newDuration(props.getSocketTimeout()))
            .maxConnections(props.getMaxConnections())
            .connectionTimeout(Beans.newDuration(props.getConnectionTimeout()))
            .connectionAcquisitionTimeout(Beans.newDuration(props.getClientExecutionTimeout()));

        if (StringUtils.isNotBlank(props.getLocalAddress())) {
            LOGGER.trace("Creating DynamoDb client local address [{}]", props.getLocalAddress());
            httpClientBuilder.localAddress(InetAddress.getByName(props.getLocalAddress()));
        }

        val clientBuilder = builder.httpClientBuilder(httpClientBuilder);
        if (clientBuilder instanceof AwsClientBuilder) {
            val overrideConfig = ClientOverrideConfiguration.builder()
                .retryPolicy(RetryMode.valueOf(props.getRetryMode()))
                .build();
            val awsClientBuilder = (AwsClientBuilder) clientBuilder;
            awsClientBuilder.overrideConfiguration(overrideConfig);
            awsClientBuilder.credentialsProvider(credentialsProvider);

            val region = props.getRegion();
            awsClientBuilder.region(StringUtils.isBlank(region) ? Region.AWS_GLOBAL : Region.of(region));

            val endpoint = props.getEndpoint();
            if (StringUtils.isNotBlank(endpoint)) {
                awsClientBuilder.endpointOverride(new URI(endpoint));
            }
        }
        return builder;
    }
}
