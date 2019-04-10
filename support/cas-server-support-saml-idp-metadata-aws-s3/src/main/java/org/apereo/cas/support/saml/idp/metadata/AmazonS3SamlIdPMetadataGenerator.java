package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
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

    public AmazonS3SamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
                                            final AmazonS3 s3Client,
                                            final String bucketName) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert() {
        return generateCertificateAndKey();
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert() {
        return generateCertificateAndKey();
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc) {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            LOGGER.trace("Bucket [{}] does not exist. Creating...", bucketName);
            val bucket = s3Client.createBucket(bucketName);
            LOGGER.debug("Created bucket [{}]", bucket.getName());
        }
        val metadata = new ObjectMetadata();
        metadata.setContentType(MediaType.TEXT_PLAIN_VALUE);
        metadata.addUserMetadata("signingCertificate", doc.getSigningCertificate());
        metadata.addUserMetadata("signingKey", doc.getSigningKey());
        metadata.addUserMetadata("encryptionCertificate", doc.getEncryptionCertificate());
        metadata.addUserMetadata("encryptionKey", doc.getEncryptionKey());
        val request = new PutObjectRequest(bucketName, String.valueOf(doc.getId()),
            new ByteArrayInputStream(doc.getMetadata().getBytes(StandardCharsets.UTF_8)), metadata);
        s3Client.putObject(request);
        return doc;
    }
}

