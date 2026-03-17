package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;

/**
 * This is {@link GoogleCloudStorageSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
public class GoogleCloudStorageSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private final Storage storage;

    public GoogleCloudStorageSamlIdPMetadataGenerator(
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
        final Storage storage) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.storage = storage;
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(_ -> generate(Optional.empty()));
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(
        final SamlIdPMetadataDocument doc,
        final Optional<SamlRegisteredService> registeredService) throws Throwable {

        doc.setAppliesTo(getAppliesToFor(registeredService));
        val bucket = createBucketIfNecessary(registeredService);

        try (val baos = new ByteArrayOutputStream()) {
            val blobId = BlobId.of(bucket.getName(), SamlIdPMetadataDocument.class.getSimpleName());
            val blobInfoBuilder = BlobInfo.newBuilder(blobId);
            registeredService.ifPresent(service ->
                blobInfoBuilder.setMetadata(Map.of(
                    "name", service.getName(),
                    "id", String.valueOf(service.getId()),
                    "entityId", service.getServiceId()
                )));
            val blobInfo = blobInfoBuilder.setContentEncoding(StandardCharsets.UTF_8.name())
                .setContentType(MediaType.APPLICATION_JSON_VALUE)
                .build();
            baos.writeBytes(doc.toJson().getBytes(StandardCharsets.UTF_8));
            storage.create(blobInfo, baos.toByteArray());
        }
        return doc;
    }

    @Override
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    protected Bucket createBucketIfNecessary(
        final Optional<SamlRegisteredService> registeredService) {
        val effectiveBucketName = getAppliesToFor(registeredService).toLowerCase(Locale.ROOT);
        var bucket = storage.get(effectiveBucketName);
        if (bucket == null) {
            LOGGER.info("Creating bucket [{}]", effectiveBucketName);
            bucket = storage.create(BucketInfo.newBuilder(effectiveBucketName).build());
        }
        return bucket;
    }
}

