package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.experimental.UtilityClass;
import lombok.val;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;

import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
public class AmazonS3SamlIdPMetadataUtils {
    /**
     * Determine bucket name for service.
     *
     * @param result     the result
     * @param bucketName the bucket name
     * @param s3Client   the s 3 client
     * @return the bucket name
     */
    public static String determineBucketNameFor(final Optional<SamlRegisteredService> result,
                                                final String bucketName,
                                                final S3Client s3Client) {
        if (result.isPresent()) {
            val bucket = getBucketForService(result.get());
            if (s3Client.listBuckets(ListBucketsRequest.builder().build())
                .buckets().stream().anyMatch(b -> b.name().equalsIgnoreCase(bucket))) {
                return bucket;
            }
        }
        return bucketName;
    }

    private static String getBucketForService(final SamlRegisteredService registeredService) {
        return registeredService.getName().toLowerCase() + registeredService.getId();
    }

}
