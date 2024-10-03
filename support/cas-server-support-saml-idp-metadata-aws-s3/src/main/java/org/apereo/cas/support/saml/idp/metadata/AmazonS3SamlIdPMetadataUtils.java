package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.CollectionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.util.Locale;
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
        return registeredService.getName().toLowerCase(Locale.ENGLISH) + registeredService.getId();
    }

    /**
     * Put saml id p metadata into bucket.
     *
     * @param s3Client        the s 3 client
     * @param doc             the doc
     * @param bucketNameToUse the bucket name to use
     * @return the saml id p metadata document
     */
    public static SamlIdPMetadataDocument putSamlIdPMetadataIntoBucket(final S3Client s3Client,
                                                                       final SamlIdPMetadataDocument doc,
                                                                       final String bucketNameToUse) {
        val metadataDetails = CollectionUtils.<String, String>wrap(
            "signingCertificate", StringUtils.defaultString(doc.getSigningCertificate()),
            "signingKey", StringUtils.defaultString(doc.getSigningKey()),
            "encryptionCertificate", StringUtils.defaultString(doc.getEncryptionCertificate()),
            "encryptionKey", StringUtils.defaultString(doc.getEncryptionKey())
        );
        metadataDetails.entrySet().removeIf(entry -> StringUtils.isBlank(entry.getValue()));
        val request = PutObjectRequest.builder()
            .key(String.valueOf(doc.getId()))
            .bucket(bucketNameToUse)
            .contentType(MediaType.TEXT_PLAIN_VALUE)
            .metadata(metadataDetails)
            .build();
        
        val requestBody = StringUtils.isBlank(doc.getMetadata()) ? RequestBody.empty() : RequestBody.fromString(doc.getMetadata());
        s3Client.putObject(request, requestBody);
        return doc;
    }
}
