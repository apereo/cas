package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

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
     * @return the bucket name
     */
    public static String determineBucketNameFor(final Optional<SamlRegisteredService> result,
                                                final String bucketName) {
        if (result.isEmpty()) {
            return bucketName;
        }
        val registeredService = result.get();
        return bucketName
            + registeredService.getName().toLowerCase()
            + registeredService.getId();
    }
}
