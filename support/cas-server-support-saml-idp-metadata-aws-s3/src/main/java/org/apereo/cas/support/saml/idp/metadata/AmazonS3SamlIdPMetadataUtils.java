package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import com.amazonaws.services.s3.AmazonS3;
import lombok.experimental.UtilityClass;
import lombok.val;

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
                                                final AmazonS3 s3Client) {
        if (result.isPresent()) {
            val bucket = getBucketForService(result.get());
            if (s3Client.doesBucketExistV2(bucket)) {
                return bucket;
            }
        }
        return bucketName;
    }

    private static String getBucketForService(final SamlRegisteredService registeredService) {
        return registeredService.getName().toLowerCase() + registeredService.getId();
    }

}
