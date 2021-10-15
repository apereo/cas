package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.DynamoDbAuditTrailManager;
import org.apereo.cas.audit.DynamoDbAuditTrailManagerFacilitator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link CasSupportDynamoDbAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casSupportDynamoDbAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportDynamoDbAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuditTrailManager dynamoDbAuditTrailManager(
        final CasConfigurationProperties casProperties,
        @Qualifier("dynamoDbAuditTrailManagerFacilitator")
        final DynamoDbAuditTrailManagerFacilitator dynamoDbAuditTrailManagerFacilitator) {
        val db = casProperties.getAudit().getDynamoDb();
        return new DynamoDbAuditTrailManager(dynamoDbAuditTrailManagerFacilitator, db.isAsynchronous());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "amazonDynamoDbAuditTrailManagerClient")
    public DynamoDbClient amazonDynamoDbAuditTrailManagerClient(final CasConfigurationProperties casProperties) {
        val db = casProperties.getAudit().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "dynamoDbAuditTrailManagerFacilitator")
    public DynamoDbAuditTrailManagerFacilitator dynamoDbAuditTrailManagerFacilitator(
        @Qualifier("amazonDynamoDbAuditTrailManagerClient")
        final DynamoDbClient amazonDynamoDbAuditTrailManagerClient,
        final CasConfigurationProperties casProperties) {
        val db = casProperties.getAudit().getDynamoDb();
        val f = new DynamoDbAuditTrailManagerFacilitator(db, amazonDynamoDbAuditTrailManagerClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer dynamoDbAuditTrailExecutionPlanConfigurer(
        @Qualifier("dynamoDbAuditTrailManager")
        final AuditTrailManager dynamoDbAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(dynamoDbAuditTrailManager);
    }
}
