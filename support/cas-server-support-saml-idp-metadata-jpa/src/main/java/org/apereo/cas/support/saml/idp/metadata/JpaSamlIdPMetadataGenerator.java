package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.Optional;

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

    public JpaSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext, final TransactionTemplate transactionTemplate) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        val results = generateCertificateAndKey();
        val doc = getSamlIdPMetadataDocument();
        doc.setEncryptionCertificate(results.getKey());
        doc.setEncryptionKey(results.getValue());
        return results;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        val results = generateCertificateAndKey();
        val doc = getSamlIdPMetadataDocument();
        doc.setSigningCertificate(results.getKey());
        doc.setSigningKey(results.getValue());
        return results;
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
    public String writeMetadata(final String metadata, final Optional<SamlRegisteredService> registeredService) {
        val doc = getSamlIdPMetadataDocument();
        doc.setMetadata(metadata);
        return metadata;
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc, final Optional<SamlRegisteredService> registeredService) {
        saveSamlIdPMetadataDocument(doc);
        return doc;
    }

    private SamlIdPMetadataDocument getSamlIdPMetadataDocument() {
        try {
            val query = this.entityManager.createQuery("SELECT r FROM SamlIdPMetadataDocument r", SamlIdPMetadataDocument.class);
            return query
                .setMaxResults(1)
                .getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage(), e);
            return new SamlIdPMetadataDocument();
        }
    }
}

