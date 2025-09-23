package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DynamoDbSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class DynamoDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
                                            final DynamoDbClient dynamoDbClient) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(__ -> generate(Optional.empty()));
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument document,
                                                               final Optional<SamlRegisteredService> registeredService) {
        document.setAppliesTo(getAppliesToFor(registeredService));
        val dynamoDb = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getMetadata().getDynamoDb();

        val values = buildTableAttributeValuesMap(document);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDb.getIdpMetadataTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, document);
        val putItemResult = dynamoDbClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return document;
    }

    @Override
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }


    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final SamlIdPMetadataDocument record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(SamlIdPMetadataColumns.APPLIES_TO.getColumnName(), AttributeValue.builder().s(record.getAppliesTo()).build());
        values.put(SamlIdPMetadataColumns.METADATA.getColumnName(), AttributeValue.builder().s(record.getMetadata()).build());
        values.put(SamlIdPMetadataColumns.SIGNING_CERTIFICATE.getColumnName(), AttributeValue.builder().s(record.getSigningCertificate()).build());
        values.put(SamlIdPMetadataColumns.ENCRYPTION_CERTIFICATE.getColumnName(), AttributeValue.builder().s(record.getEncryptionCertificate()).build());
        values.put(SamlIdPMetadataColumns.SIGNING_KEY.getColumnName(), AttributeValue.builder().s(record.getSigningKey()).build());
        values.put(SamlIdPMetadataColumns.ENCRYPTION_KEY.getColumnName(), AttributeValue.builder().s(record.getEncryptionKey()).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }
}
