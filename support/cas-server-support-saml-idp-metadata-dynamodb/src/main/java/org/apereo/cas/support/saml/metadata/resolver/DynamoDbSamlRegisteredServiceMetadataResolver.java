package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * This is {@link DynamoDbSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class DynamoDbSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                         final OpenSamlConfigBean configBean,
                                                         final DynamoDbClient dynamoDbClient) {
        super(samlIdPProperties, configBean);
        this.dynamoDbClient = dynamoDbClient;
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(SamlRegisteredServiceMetadataColumns.NAME.getColumnName())
                .operator(ComparisonOperator.NOT_NULL)
                .build());
        val documents = getRecordsByKeys(query);
        return documents
            .map(doc -> buildMetadataResolverFrom(service, doc))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Stream<SamlMetadataDocument> getRecordsByKeys(final List<? extends DynamoDbQueryBuilder> queries) {
        val dynamoDbProperties = samlIdPProperties.getMetadata().getDynamoDb();
        return DynamoDbTableUtils.getRecordsByKeys(dynamoDbClient,
            dynamoDbProperties.getTableName(),
            queries,
            DynamoDbSamlRegisteredServiceMetadataResolver::extractAttributeValuesFrom);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        if (service != null) {
            val metadataLocation = service.getMetadataLocation();
            return StringUtils.isNotBlank(metadataLocation) && metadataLocation.trim().startsWith(getSourceId());
        }
        return false;
    }

    @Override
    public String getSourceId() {
        return "dynamodb://";
    }

    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        val dynamoDb = samlIdPProperties.getMetadata().getDynamoDb();
        val values = buildTableAttributeValuesMap(document.assignIdIfNecessary());
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDb.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, document);
        val putItemResult = dynamoDbClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return document;
    }

    @Override
    public List<SamlMetadataDocument> load() {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(SamlRegisteredServiceMetadataColumns.NAME.getColumnName())
                .operator(ComparisonOperator.NOT_NULL)
                .build());
        return getRecordsByKeys(query).collect(Collectors.toList());
    }

    @Override
    public void removeById(final long id) {
        findById(id).ifPresent(doc -> removeByName(doc.getName()));
    }

    @Override
    public void removeByName(final String name) {
        val dynamoDb = samlIdPProperties.getMetadata().getDynamoDb();
        val del = DeleteItemRequest.builder()
            .tableName(dynamoDb.getTableName())
            .key(Map.of(SamlRegisteredServiceMetadataColumns.NAME.getColumnName(),
                AttributeValue.builder().s(name).build()))
            .build();
        LOGGER.debug("Submitting delete request [{}] for document [{}]", del, name);
        dynamoDbClient.deleteItem(del);
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(SamlRegisteredServiceMetadataColumns.NAME.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(name).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        return getRecordsByKeys(query).findFirst();
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(SamlRegisteredServiceMetadataColumns.ID.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().n(String.valueOf(id)).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        return getRecordsByKeys(query).findFirst();
    }

    @Override
    public void removeAll() {
        val dynamoDb = samlIdPProperties.getMetadata().getDynamoDb();
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(SamlRegisteredServiceMetadataColumns.NAME.getColumnName())
                .operator(ComparisonOperator.NOT_NULL)
                .build());
        val scanResult = DynamoDbTableUtils.scan(dynamoDbClient, dynamoDb.getTableName(), query);
        scanResult.items().forEach(item -> {
            val name = item.get(SamlRegisteredServiceMetadataColumns.NAME.getColumnName());
            if (name != null) {
                val del = DeleteItemRequest.builder()
                    .tableName(dynamoDb.getTableName())
                    .key(Map.of(SamlRegisteredServiceMetadataColumns.NAME.getColumnName(), name))
                    .build();
                dynamoDbClient.deleteItem(del);
            }
        });
    }

    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final SamlMetadataDocument record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(SamlRegisteredServiceMetadataColumns.ID.getColumnName(),
            AttributeValue.builder().n(String.valueOf(record.getId())).build());
        values.put(SamlRegisteredServiceMetadataColumns.NAME.getColumnName(), AttributeValue.builder().s(record.getName()).build());
        values.put(SamlRegisteredServiceMetadataColumns.VALUE.getColumnName(), AttributeValue.builder().s(record.getValue()).build());
        if (StringUtils.isNotBlank(record.getSignature())) {
            values.put(SamlRegisteredServiceMetadataColumns.SIGNATURE.getColumnName(), AttributeValue.builder().s(record.getSignature()).build());
        }
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    private static SamlMetadataDocument extractAttributeValuesFrom(final Map<String, AttributeValue> item) {
        val builder = SamlMetadataDocument.builder()
            .name(item.get(SamlRegisteredServiceMetadataColumns.NAME.getColumnName()).s())
            .value(item.get(SamlRegisteredServiceMetadataColumns.VALUE.getColumnName()).s())
            .id(Long.parseLong(item.get(SamlRegisteredServiceMetadataColumns.ID.getColumnName()).n()));
        val signature = item.get(SamlRegisteredServiceMetadataColumns.SIGNATURE.getColumnName());
        if (signature != null) {
            builder.signature(signature.s());
        }
        return builder.build();
    }
}
