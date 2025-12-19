package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AmazonFirehoseAuditTrailManager;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.firehose.FirehoseClient;

/**
 * This is {@link CasSupportAmazonFirehoseAuditAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit, module = "aws-firehose")
@AutoConfiguration
public class CasSupportAmazonFirehoseAuditAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "awsFirehoseAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailManager awsFirehoseAuditTrailManager(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("awsFirehoseAuditClient")
        final FirehoseClient awsFirehoseAuditClient,
        final CasConfigurationProperties casProperties) {
        val amazonFirehose = casProperties.getAudit().getAmazonFirehose();
        return new AmazonFirehoseAuditTrailManager(awsFirehoseAuditClient,
            new AuditActionContextJsonSerializer(applicationContext),
            amazonFirehose);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "awsFirehoseAuditClient")
    public FirehoseClient awsFirehoseAuditClient(final CasConfigurationProperties casProperties) {
        val amz = casProperties.getAudit().getAmazonFirehose();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());
        val builder = FirehoseClient.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, credentials, amz);
        return builder.build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "awsFirehoseAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer awsFirehoseAuditTrailExecutionPlanConfigurer(
        @Qualifier("awsFirehoseAuditTrailManager")
        final AuditTrailManager awsFirehoseAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(awsFirehoseAuditTrailManager);
    }
}
