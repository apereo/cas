package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.jpa.JpaSamlIdPMetadataDocumentFactory;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

/**
 * This is {@link JpaSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerSamlMetadataIdP")
public class JpaSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private final TransactionOperations transactionTemplate;

    @PersistenceContext(unitName = "jpaSamlMetadataIdPContext")
    private EntityManager entityManager;

    public JpaSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                       final TransactionOperations transactionTemplate) {
        super(context);
        this.transactionTemplate = transactionTemplate;
    }

    private void saveSamlIdPMetadataDocument(final SamlIdPMetadataDocument doc) {
        this.transactionTemplate.executeWithoutResult(_ -> entityManager.merge(doc));
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(_ -> generate(Optional.empty()));
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
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }
}

