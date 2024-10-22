package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
@Slf4j
public class AmazonS3SamlIdPMetadataUtils {
    private static final String NEW_LINE_REPLACEMENT = "<br/>";

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
                .buckets().stream().anyMatch(foundBucket -> foundBucket.name().equalsIgnoreCase(bucket))) {
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
            "signingCertificate", sanitizeNewline(StringUtils.defaultString(doc.getSigningCertificate())),
            "signingKey", sanitizeNewline(StringUtils.defaultString(doc.getSigningKey())),
            "encryptionCertificate", sanitizeNewline(StringUtils.defaultString(doc.getEncryptionCertificate())),
            "encryptionKey", sanitizeNewline(StringUtils.defaultString(doc.getEncryptionKey()))
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

    /**
     * Read metadata document from bucket.
     *
     * @param object      the object
     * @param bucketToUse the bucket to use
     * @return the saml id p metadata document
     */
    public static SamlIdPMetadataDocument readMetadataDocumentFromBucket(final ResponseInputStream<GetObjectResponse> object,
                                                                         final String bucketToUse) {
        val metadataDocument = new SamlIdPMetadataDocument();
        metadataDocument.setMetadata(FunctionUtils.doUnchecked(() -> IOUtils.toString(object, StandardCharsets.UTF_8)));
        val objectMetadata = object.response().metadata();
        LOGGER.debug("Located S3 object metadata [{}] from bucket [{}]", objectMetadata, bucketToUse);
        metadataDocument.setEncryptionCertificate(restoreNewline(getObjectMetadataEntry(objectMetadata, "encryptionCertificate")));
        metadataDocument.setSigningCertificate(restoreNewline(getObjectMetadataEntry(objectMetadata, "signingCertificate")));
        metadataDocument.setEncryptionKey(restoreNewline(getObjectMetadataEntry(objectMetadata, "encryptionKey")));
        metadataDocument.setSigningKey(restoreNewline(getObjectMetadataEntry(objectMetadata, "signingKey")));
        metadataDocument.setAppliesTo(bucketToUse);
        return metadataDocument;
    }

    private static String sanitizeNewline(final String s) {
        val replaced = StringUtils.replace(s, "\n", NEW_LINE_REPLACEMENT);
        LOGGER.trace("Sanitized [{}] => [{}]", s, replaced);
        return replaced;
    }

    private static String restoreNewline(final String s) {
        val replaced = StringUtils.replace(s, NEW_LINE_REPLACEMENT, "\n");
        LOGGER.trace("Restored [{}] => [{}]", s, replaced);
        return replaced;
    }

    private static String getObjectMetadataEntry(final Map<String, String> objectMetadata,
                                                 final String key) {
        return StringUtils.defaultIfBlank(objectMetadata.get(key),
            objectMetadata.get(key.toLowerCase(Locale.ENGLISH)));
    }
}
