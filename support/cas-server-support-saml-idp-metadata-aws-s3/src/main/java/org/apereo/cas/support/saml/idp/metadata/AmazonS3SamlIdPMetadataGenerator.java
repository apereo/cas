package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonS3SamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    private final transient AmazonS3 s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                            final AmazonS3 s3Client, final String bucketName) {
        super(context);
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        val bucketNameToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService, this.bucketName, s3Client);
        if (!s3Client.doesBucketExistV2(bucketNameToUse)) {
            LOGGER.trace("Bucket [{}] does not exist. Creating...", bucketNameToUse);
            val bucket = s3Client.createBucket(bucketNameToUse);
            LOGGER.debug("Created bucket [{}]", bucket.getName());
        }
        val metadata = new ObjectMetadata();
        metadata.setContentType(MediaType.TEXT_PLAIN_VALUE);
        metadata.addUserMetadata("signingCertificate", doc.getSigningCertificate());
        metadata.addUserMetadata("signingKey", doc.getSigningKey());
        metadata.addUserMetadata("encryptionCertificate", doc.getEncryptionCertificate());
        metadata.addUserMetadata("encryptionKey", doc.getEncryptionKey());
        val request = new PutObjectRequest(bucketNameToUse, String.valueOf(doc.getId()),
            new ByteArrayInputStream(doc.getMetadata().getBytes(StandardCharsets.UTF_8)), metadata);
        s3Client.putObject(request);
        return doc;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }
}

