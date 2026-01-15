package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ConfigurableApplicationContext;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

/**
 * This is {@link AmazonS3SamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Monitorable
public class AmazonS3SamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final S3Client s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                          final Cache<@NonNull String, SamlIdPMetadataDocument> metadataCache,
                                          final String bucketName, final S3Client s3Client,
                                          final ConfigurableApplicationContext applicationContext) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.bucketName = SpringExpressionLanguageValueResolver.getInstance().resolve(bucketName);
        this.s3Client = s3Client;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) throws Exception {
        val bucketToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService, this.bucketName, s3Client);
        LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketToUse);
        if (s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets().stream().noneMatch(b -> b.name().equalsIgnoreCase(bucketToUse))) {
            LOGGER.debug("S3 bucket [{}] does not exist", bucketToUse);
            return null;
        }

        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketToUse).build());
        val objects = result.contents();
        LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketToUse);

        if (objects.isEmpty()) {
            return null;
        }
        if (objects.size() > 1) {
            LOGGER.warn("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketToUse);
            objects.forEach(obj -> {
                LOGGER.debug("Deleting object [{}] from bucket [{}]", obj.key(), bucketToUse);
                val deleteRequest = DeleteObjectRequest.builder().bucket(bucketToUse).key(obj.key()).build();
                s3Client.deleteObject(deleteRequest);
            });
            throw new IllegalArgumentException("Multiple S3 objects where found in bucket " + bucketToUse);
        }

        val firstMetadataObject = objects.getFirst();
        LOGGER.debug("Fetching object [{}] from bucket [{}]", firstMetadataObject.key(), bucketToUse);
        val metadataEntry = s3Client.getObject(GetObjectRequest.builder().bucket(bucketToUse).key(firstMetadataObject.key()).build());
        return AmazonS3SamlIdPMetadataUtils.readMetadataDocumentFromBucket(metadataEntry, bucketToUse);
    }
}

