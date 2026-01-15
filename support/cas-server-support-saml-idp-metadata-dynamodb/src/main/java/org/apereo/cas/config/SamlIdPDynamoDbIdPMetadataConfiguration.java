package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.support.saml.idp.metadata.DynamoDbSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.DynamoDbSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.DynamoDbSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.SamlIdPMetadataColumns;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * This is {@link SamlIdPDynamoDbIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProviderMetadata, module = "dynamodb")
@Configuration(value = "SamlIdPDynamoDbIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPDynamoDbIdPMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.dynamo-db.idp-metadata-table-name");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CipherExecutor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                val crypto = idp.getMetadata().getDynamoDb().getCrypto();
                if (crypto.isEnabled()) {
                    return CipherExecutorUtils.newStringCipherExecutor(crypto, DynamoDbSamlIdPMetadataCipherExecutor.class);
                }
                LOGGER.info("DynamoDb SAML IdP metadata encryption/signing is turned off and "
                            + "MAY NOT be safe in a production environment. "
                            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
                return CipherExecutor.noOp();
            })
            .otherwise(CipherExecutor::noOp)
            .get();
    }


    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbSamlIdPMetadataClient")
        final DynamoDbClient amazonDynamoDBClient,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return BeanSupplier.of(SamlIdPMetadataGenerator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DynamoDbSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, amazonDynamoDBClient))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("samlIdPMetadataCache")
        final Cache<@NonNull String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("amazonDynamoDbSamlIdPMetadataClient")
        final DynamoDbClient amazonDynamoDBClient) {
        return BeanSupplier.of(SamlIdPMetadataLocator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DynamoDbSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
                samlIdPMetadataCache, amazonDynamoDBClient, applicationContext, casProperties))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbSamlIdPMetadataClient")
    public DynamoDbClient amazonDynamoDbSamlIdPMetadataClient(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(DynamoDbClient.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supplyUnchecked(() -> {
                val dynamoDbProperties = casProperties.getAuthn().getSamlIdp().getMetadata().getDynamoDb();
                val factory = new AmazonDynamoDbClientFactory();
                val client = factory.createAmazonDynamoDb(dynamoDbProperties);
                if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
                    val attributes = List.of(AttributeDefinition.builder()
                        .attributeName(SamlIdPMetadataColumns.APPLIES_TO.getColumnName())
                        .attributeType(ScalarAttributeType.S)
                        .build());
                    val schema = List.of(KeySchemaElement.builder()
                        .attributeName(SamlIdPMetadataColumns.APPLIES_TO.getColumnName())
                        .keyType(KeyType.HASH)
                        .build());
                    DynamoDbTableUtils.createTable(client, dynamoDbProperties,
                        dynamoDbProperties.getIdpMetadataTableName(), dynamoDbProperties.isDropTablesOnStartup(),
                        attributes, schema);
                }
                return client;
            })
            .otherwiseProxy()
            .get();
    }

}
