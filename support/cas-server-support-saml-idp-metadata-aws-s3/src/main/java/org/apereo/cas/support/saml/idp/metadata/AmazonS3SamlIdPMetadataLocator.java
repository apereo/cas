package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonS3SamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final transient AmazonS3 s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                          final String bucketName, final AmazonS3 s3Client) {
        super(metadataCipherExecutor);
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = new SamlIdPMetadataDocument();

        try {
            val bucketToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService, this.bucketName, s3Client);
            LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketToUse);
            if (!s3Client.doesBucketExistV2(bucketToUse)) {
                LOGGER.debug("S3 bucket [{}] does not exist", bucketToUse);
                return metadataDocument;
            }

            val result = s3Client.listObjectsV2(bucketToUse);
            val objects = result.getObjectSummaries();
            LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketToUse);

            if (objects.isEmpty()) {
                throw new IllegalArgumentException("No objects found in bucket " + bucketToUse);
            }
            val obj = objects.get(0);
            val objectKey = obj.getKey();
            LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketToUse);
            val object = s3Client.getObject(obj.getBucketName(), objectKey);

            val objectMetadata = object.getObjectMetadata();
            if (objectMetadata != null) {
                metadataDocument.setEncryptionCertificate(objectMetadata.getUserMetaDataOf("encryptionCertificate"));
                metadataDocument.setSigningCertificate(objectMetadata.getUserMetaDataOf("signingCertificate"));
                metadataDocument.setEncryptionKey(objectMetadata.getUserMetaDataOf("encryptionKey"));
                metadataDocument.setSigningKey(objectMetadata.getUserMetaDataOf("signingKey"));
                metadataDocument.setAppliesTo(bucketToUse);
            }

            try (val is = object.getObjectContent()) {
                metadataDocument.setMetadata(IOUtils.toString(is));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return metadataDocument;
    }
}

