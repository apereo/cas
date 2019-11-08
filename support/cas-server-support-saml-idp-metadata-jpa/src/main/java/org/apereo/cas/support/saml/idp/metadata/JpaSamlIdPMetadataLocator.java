package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.Optional;

/**
 * This is {@link JpaSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */

@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerSamlMetadataIdP")
@Slf4j
public class JpaSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    @PersistenceContext(unitName = "samlMetadataIdPEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor) {
        super(metadataCipherExecutor);
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        try {
            val query = this.entityManager.createQuery("SELECT r FROM SamlIdPMetadataDocument r", SamlIdPMetadataDocument.class);
            return query.setMaxResults(1).getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new SamlIdPMetadataDocument();
    }
}

