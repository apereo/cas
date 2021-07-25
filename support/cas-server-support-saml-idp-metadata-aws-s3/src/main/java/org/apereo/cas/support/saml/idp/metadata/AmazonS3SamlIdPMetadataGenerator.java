package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonS3SamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    private final transient S3Client s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                            final S3Client s3Client, final String bucketName) {
        super(context);
        this.s3Client = s3Client;
        this.bucketName = SpringExpressionLanguageValueResolver.getInstance().resolve(bucketName);
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        val bucketNameToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService, this.bucketName, s3Client);
        if (s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets().stream().noneMatch(b -> b.name().equalsIgnoreCase(bucketNameToUse))) {
            LOGGER.trace("Bucket [{}] does not exist. Creating...", bucketNameToUse);
            val bucket = s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketNameToUse).build());
            LOGGER.debug("Created bucket [{}]", bucket.location());
        }
        val request = PutObjectRequest.builder()
            .key(String.valueOf(doc.getId()))
            .bucket(bucketNameToUse)
            .contentType(MediaType.TEXT_PLAIN_VALUE)
            .metadata(Map.of(
                "signingCertificate", doc.getSigningCertificate(),
                "signingKey", doc.getSigningKey(),
                "encryptionCertificate", doc.getEncryptionCertificate(),
                "encryptionKey", doc.getEncryptionKey()))
            .build();
        s3Client.putObject(request, RequestBody.fromString(doc.getMetadata()));
        return doc;
    }
}

