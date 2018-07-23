package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.StringWriter;

/**
 * This is {@link JpaSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerSamlMetadataIdP")
@Slf4j
public class JpaSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    @PersistenceContext(unitName = "samlMetadataIdPEntityManagerFactory")
    private transient EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;
    private final CipherExecutor<String, String> metadataCipherExecutor;

    public JpaSamlIdPMetadataGenerator(final SamlIdPMetadataLocator samlIdPMetadataLocator,
                                       final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter,
                                       final String entityId,
                                       final ResourceLoader resourceLoader,
                                       final String casServerPrefix,
                                       final String scope,
                                       final CipherExecutor metadataCipherExecutor,
                                       final TransactionTemplate transactionTemplate) {
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, entityId, resourceLoader, casServerPrefix, scope);
        this.metadataCipherExecutor = metadataCipherExecutor;
        this.transactionTemplate = transactionTemplate;
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

    private void saveSamlIdPMetadataDocument(final SamlIdPMetadataDocument doc) {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                entityManager.merge(doc);
            }
        });
    }

    @Override
    public void writeMetadata(final String metadata) {
        val doc = getSamlIdPMetadataDocument();
        doc.setMetadata(metadata);
        saveSamlIdPMetadataDocument(doc);
    }

    private SamlIdPMetadataDocument getSamlIdPMetadataDocument() {
        try {
            return this.entityManager.createQuery("SELECT r FROM SamlIdPMetadataDocument r", SamlIdPMetadataDocument.class)
                .setMaxResults(1)
                .getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage(), e);
            return new SamlIdPMetadataDocument();
        }
    }
}

