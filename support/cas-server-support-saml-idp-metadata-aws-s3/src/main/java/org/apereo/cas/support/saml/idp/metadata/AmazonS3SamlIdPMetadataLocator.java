package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonS3SamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final transient S3Client s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                          final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                          final String bucketName, final S3Client s3Client) {
        super(metadataCipherExecutor, metadataCache);
        this.bucketName = SpringExpressionLanguageValueResolver.getInstance().resolve(bucketName);
        this.s3Client = s3Client;
    }

    @Override
    @SneakyThrows
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = new SamlIdPMetadataDocument();

        val bucketToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService, this.bucketName, s3Client);
        LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketToUse);
        if (s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets().stream().noneMatch(b -> b.name().equalsIgnoreCase(bucketToUse))) {
            LOGGER.debug("S3 bucket [{}] does not exist", bucketToUse);
            return metadataDocument;
        }

        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketToUse).build());
        val objects = result.contents();
        LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketToUse);

        if (objects.isEmpty()) {
            throw new IllegalArgumentException("No objects found in bucket " + bucketToUse);
        }
        val obj = objects.get(0);
        val objectKey = obj.key();
        LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketToUse);
        val object = s3Client.getObject(GetObjectRequest.builder().bucket(bucketToUse).key(objectKey).build());

        metadataDocument.setMetadata(IOUtils.toString(object, StandardCharsets.UTF_8));
        val objectMetadata = object.response().metadata();
        metadataDocument.setEncryptionCertificate(objectMetadata.get("encryptionCertificate"));
        metadataDocument.setSigningCertificate(objectMetadata.get("signingCertificate"));
        metadataDocument.setEncryptionKey(objectMetadata.get("encryptionKey"));
        metadataDocument.setSigningKey(objectMetadata.get("signingKey"));
        metadataDocument.setAppliesTo(bucketToUse);
        return metadataDocument;
    }
}

