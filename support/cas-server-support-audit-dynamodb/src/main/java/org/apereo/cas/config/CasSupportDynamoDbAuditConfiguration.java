package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.DynamoDbAuditTrailManager;
import org.apereo.cas.audit.DynamoDbAuditTrailManagerFacilitator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportDynamoDbAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casSupportDynamoDbAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportDynamoDbAuditConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbAuditTrailManager")
    @RefreshScope
    public AuditTrailManager dynamoDbAuditTrailManager() {
        val db = casProperties.getAudit().getDynamoDb();
        return new DynamoDbAuditTrailManager(dynamoDbAuditTrailManagerFacilitator(), db.isAsynchronous());
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "amazonDynamoDbAuditTrailManagerClient")
    public AmazonDynamoDB amazonDynamoDbAuditTrailManagerClient() {
        val db = casProperties.getAudit().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbAuditTrailManagerFacilitator")
    public DynamoDbAuditTrailManagerFacilitator dynamoDbAuditTrailManagerFacilitator() {
        val db = casProperties.getAudit().getDynamoDb();
        val f = new DynamoDbAuditTrailManagerFacilitator(db, amazonDynamoDbAuditTrailManagerClient());
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer dynamoDbAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(dynamoDbAuditTrailManager());
    }
}
