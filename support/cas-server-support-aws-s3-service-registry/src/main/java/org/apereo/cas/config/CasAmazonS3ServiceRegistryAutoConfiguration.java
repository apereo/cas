package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.aws.s3.services.AmazonS3ServiceRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasAmazonS3ServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "s3")
@AutoConfiguration
public class CasAmazonS3ServiceRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amazonS3ServiceRegistryClient")
    public S3Client amazonS3ServiceRegistryClient(final CasConfigurationProperties casProperties) {
        val amz = casProperties.getServiceRegistry().getAmazonS3();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());
        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(amz.isPathStyleEnabled())
            .build();
        val builder = S3Client.builder();
        builder.serviceConfiguration(s3Config);
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, credentials, amz);
        return builder.build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amazonS3ServiceRegistry")
    public ServiceRegistry amazonS3ServiceRegistry(
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
        @Qualifier("amazonS3ServiceRegistryClient")
        final S3Client amazonS3ServiceRegistryClient, final ConfigurableApplicationContext applicationContext) {
        return new AmazonS3ServiceRegistry(applicationContext,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new), amazonS3ServiceRegistryClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "amazonS3ServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer amazonS3ServiceRegistryExecutionPlanConfigurer(
        @Qualifier("amazonS3ServiceRegistry")
        final ServiceRegistry amazonS3ServiceRegistry) {
        return plan -> plan.registerServiceRegistry(amazonS3ServiceRegistry);
    }
}
