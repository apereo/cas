package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.ResourceLoader;

import java.io.StringWriter;
import java.math.BigInteger;

/**
 * This is {@link AmazonS3SamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AmazonS3SamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {

    private final CipherExecutor<String, String> metadataCipherExecutor;

    private final transient AmazonS3 s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataGenerator(final SamlIdPMetadataLocator samlIdPMetadataLocator,
                                            final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter,
                                            final String entityId,
                                            final ResourceLoader resourceLoader, final String casServerPrefix,
                                            final String scope,
                                            final CipherExecutor metadataCipherExecutor,
                                            final AmazonS3 s3Client,
                                            final String bucketName) {
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, entityId, resourceLoader, casServerPrefix, scope);
        this.metadataCipherExecutor = metadataCipherExecutor;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedEncryptionCert() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val encryptionKey = metadataCipherExecutor.encode(keyWriter.toString());
            val doc = getSamlIdPMetadataDocument();
            doc.setEncryptionCertificate(certWriter.toString());
            doc.setEncryptionKey(encryptionKey);
            saveSamlIdPMetadataDocument(doc);
        }
    }

    private SamlIdPMetadataDocument getSamlIdPMetadataDocument() {
        val doc = new SamlIdPMetadataDocument();
        doc.setId(BigInteger.valueOf(System.nanoTime()));
        return doc;
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedSigningCert() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val signingKey = metadataCipherExecutor.encode(keyWriter.toString());
            val doc = getSamlIdPMetadataDocument();
            doc.setSigningCertificate(certWriter.toString());
            doc.setSigningKey(signingKey);
            saveSamlIdPMetadataDocument(doc);
        }
    }

    @Override
    protected void writeMetadata(final String metadata) {
        val doc = getSamlIdPMetadataDocument();
        doc.setMetadata(metadata);
        saveSamlIdPMetadataDocument(doc);
    }

    private void saveSamlIdPMetadataDocument(final SamlIdPMetadataDocument doc) {
        val request = new PutObjectRequest(bucketName, doc.getId().toString(), doc.getMetadata());
        val metadata = new ObjectMetadata();

        metadata.setContentType("plain/text");
        metadata.addUserMetadata("encryptionCertificate", doc.getEncryptionCertificate());
        metadata.addUserMetadata("signingCertificate", doc.getSigningCertificate());
        metadata.addUserMetadata("encryptionKey", doc.getEncryptionKey());
        metadata.addUserMetadata("signingKey", doc.getSigningKey());
        request.setMetadata(metadata);
        s3Client.putObject(request);
    }

}

