package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.jpa.JpaSamlIdPMetadataDocumentFactory;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
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
public class JpaSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext(unitName = "samlMetadataIdPEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                       final TransactionTemplate transactionTemplate) {
        super(context);
        this.transactionTemplate = transactionTemplate;
    }

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
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
    public void afterPropertiesSet() {
        generate(Optional.empty());
    }

    @Override
    protected SamlIdPMetadataDocument newSamlIdPMetadataDocument() {
        val jpa = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getMetadata().getJpa();
        return new JpaSamlIdPMetadataDocumentFactory(jpa.getDialect()).newInstance();
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        doc.setAppliesTo(getAppliesToFor(registeredService));
        saveSamlIdPMetadataDocument(doc);
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

