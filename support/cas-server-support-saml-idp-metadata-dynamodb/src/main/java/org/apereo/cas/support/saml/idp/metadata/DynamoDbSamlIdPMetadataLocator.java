package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@Monitorable
public class DynamoDbSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final DynamoDbClient dynamoDbClient;
    private final CasConfigurationProperties casProperties;

    public DynamoDbSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                          final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                          final DynamoDbClient dynamoDbClient,
                                          final ConfigurableApplicationContext applicationContext,
                                          final CasConfigurationProperties casProperties) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.dynamoDbClient = dynamoDbClient;
        this.casProperties = casProperties;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = getAppliesToFor(registeredService);
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(SamlIdPMetadataColumns.APPLIES_TO.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(appliesTo).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        return getRecordsByKeys(query).findFirst().orElse(null);
    }

    private Stream<SamlIdPMetadataDocument> getRecordsByKeys(final List<? extends DynamoDbQueryBuilder> queries) {
        val dynamoDbProperties = casProperties.getAuthn().getSamlIdp().getMetadata().getDynamoDb();
        return DynamoDbTableUtils.getRecordsByKeys(dynamoDbClient,
            dynamoDbProperties.getIdpMetadataTableName(),
            queries,
            DynamoDbSamlIdPMetadataLocator::extractAttributeValuesFrom);
    }

    private static SamlIdPMetadataDocument extractAttributeValuesFrom(final Map<String, AttributeValue> item) {
        return SamlIdPMetadataDocument.builder()
            .appliesTo(item.get(SamlIdPMetadataColumns.APPLIES_TO.getColumnName()).s())
            .metadata(item.get(SamlIdPMetadataColumns.METADATA.getColumnName()).s())
            .signingCertificate(item.get(SamlIdPMetadataColumns.SIGNING_CERTIFICATE.getColumnName()).s())
            .encryptionCertificate(item.get(SamlIdPMetadataColumns.ENCRYPTION_CERTIFICATE.getColumnName()).s())
            .signingKey(item.get(SamlIdPMetadataColumns.SIGNING_KEY.getColumnName()).s())
            .encryptionKey(item.get(SamlIdPMetadataColumns.ENCRYPTION_KEY.getColumnName()).s())
            .build();
    }
}
