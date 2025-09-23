package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.DynamoDbSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.metadata.resolver.SamlRegisteredServiceMetadataColumns;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import java.util.List;

/**
 * This is {@link SamlIdPDynamoDbIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLServiceProviderMetadata, module = "dynamodb")
@Configuration(value = "SamlIdPDynamoDbRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPDynamoDbRegisteredServiceMetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbSamlRegisteredServiceMetadataResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolver dynamoDbSamlRegisteredServiceMetadataResolver(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbSamlRegisteredServiceMetadataClient")
        final DynamoDbClient amazonDynamoDbSamlRegisteredServiceMetadataClient,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new DynamoDbSamlRegisteredServiceMetadataResolver(idp,
            openSamlConfigBean, amazonDynamoDbSamlRegisteredServiceMetadataClient);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbSamlRegisteredServiceMetadataClient")
    public DynamoDbClient amazonDynamoDbSamlRegisteredServiceMetadataClient(
        final CasConfigurationProperties casProperties) throws Exception {
        val dynamoDbProperties = casProperties.getAuthn().getSamlIdp().getMetadata().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        val client = factory.createAmazonDynamoDb(dynamoDbProperties);
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            val attributes = List.of(AttributeDefinition.builder()
                .attributeName(SamlRegisteredServiceMetadataColumns.NAME.getColumnName())
                .attributeType(ScalarAttributeType.S)
                .build());
            val schema = List.of(KeySchemaElement.builder()
                .attributeName(SamlRegisteredServiceMetadataColumns.NAME.getColumnName())
                .keyType(KeyType.HASH)
                .build());
            DynamoDbTableUtils.createTable(client, dynamoDbProperties,
                dynamoDbProperties.getTableName(), dynamoDbProperties.isDropTablesOnStartup(),
                attributes, schema);
        }
        return client;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "dynamoDbSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer dynamoDbSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("dynamoDbSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver dynamoDbSamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(dynamoDbSamlRegisteredServiceMetadataResolver);
    }
}
