package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
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

@EnableTransactionManagement
@Transactional(transactionManager = "transactionManagerSamlMetadataIdP")
@Slf4j
@Getter
public class JpaSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    @PersistenceContext(unitName = "samlMetadataIdPEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                     final Cache<String, SamlIdPMetadataDocument> metadataCache) {
        super(metadataCipherExecutor, metadataCache);
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

    /**
     * Build query.
     *
     * @param registeredService the registered service
     * @return the typed query
     */
    protected TypedQuery<SamlIdPMetadataDocument> buildQuery(final Optional<SamlRegisteredService> registeredService) {
        var sql = "SELECT r FROM SamlIdPMetadataDocument r ";
        if (registeredService.isPresent()) {
            sql += " WHERE r.appliesTo = :appliesTo";
        }
        val query = getEntityManager().createQuery(sql, SamlIdPMetadataDocument.class);
        if (registeredService.isPresent()) {
            query.setParameter("appliesTo", SamlIdPMetadataGenerator.getAppliesToFor(registeredService));
        }
        return query.setMaxResults(1);
    }
}

