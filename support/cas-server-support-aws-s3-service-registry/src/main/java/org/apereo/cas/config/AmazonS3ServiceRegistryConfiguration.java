package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.aws.s3.services.AmazonS3ServiceRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Collection;

/**
 * This is {@link AmazonS3ServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "AmazonS3ServiceRegistryConfiguration", proxyBeanMethods = false)
public class AmazonS3ServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "amazonS3ServiceRegistryClient")
    public S3Client amazonS3ServiceRegistryClient() {
        val amz = casProperties.getServiceRegistry().getAmazonS3();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());
        val builder = S3Client.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder, credentials, amz);
        return builder.build();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "amazonS3ServiceRegistry")
    @Autowired
    public ServiceRegistry amazonS3ServiceRegistry(@Qualifier("amazonS3ServiceRegistryClient") final S3Client amazonS3ServiceRegistryClient) {
        return new AmazonS3ServiceRegistry(applicationContext,
            serviceRegistryListeners.getObject(),
            amazonS3ServiceRegistryClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "amazonS3ServiceRegistryExecutionPlanConfigurer")
    @RefreshScope
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer amazonS3ServiceRegistryExecutionPlanConfigurer(@Qualifier("amazonS3ServiceRegistry")
                                                                                                 final ServiceRegistry amazonS3ServiceRegistry) {
        return plan -> plan.registerServiceRegistry(amazonS3ServiceRegistry);
    }

}
