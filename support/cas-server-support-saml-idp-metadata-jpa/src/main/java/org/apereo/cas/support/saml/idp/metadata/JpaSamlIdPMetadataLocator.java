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
import javax.persistence.TypedQuery;

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

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        try {
            if (registeredService.isPresent()) {
                val query = buildQuery(registeredService);
                val results = query.getResultList();
                if (!results.isEmpty()) {
                    return results.get(0);
                }
            }
            return buildQuery(Optional.empty()).getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new SamlIdPMetadataDocument();
    }

    private TypedQuery<SamlIdPMetadataDocument> buildQuery(final Optional<SamlRegisteredService> registeredService) {
        var sql = "SELECT r FROM SamlIdPMetadataDocument r ";
        if (registeredService.isPresent()) {
            sql += " WHERE r.appliesTo = :appliesTo";
        }
        val query = this.entityManager.createQuery(sql, SamlIdPMetadataDocument.class);
        if (registeredService.isPresent()) {
            query.setParameter("appliesTo", getAppliesToFor(registeredService));
        }
        return query.setMaxResults(1);
    }
}

