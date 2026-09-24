package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 * This is {@link JpaSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */

@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerSamlMetadataIdP")
@Slf4j
@Getter
@Monitorable
public class JpaSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    @PersistenceContext(unitName = "jpaSamlMetadataIdPContext")
    private EntityManager entityManager;

    public JpaSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                     final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                     final ConfigurableApplicationContext applicationContext) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        try {
            if (registeredService.isPresent()) {
                val query = buildQuery(registeredService);
                val results = query.getResultList();
                if (!results.isEmpty()) {
                    return results.getFirst();
                }
            }
            return buildQuery(Optional.empty()).getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new SamlIdPMetadataDocument();
    }

    /**
     * Build a query scoped to the owner of the document.
     * <p>
     * Per-service and global documents share one table, distinguished only by {@code appliesTo}, so an
     * unscoped query is not "the global document" -- it is whichever row the database happens to return
     * first, which can be a service's own document and, through it, that service's signing and
     * encryption keys. The global owner has a name of its own, so it is queried by name like any other.
     *
     * @param registeredService the registered service, or empty for the global document
     * @return the typed query
     */
    protected TypedQuery<SamlIdPMetadataDocument> buildQuery(final Optional<SamlRegisteredService> registeredService) {
        val query = getEntityManager().createQuery(
            "SELECT r FROM SamlIdPMetadataDocument r WHERE r.appliesTo = :appliesTo", SamlIdPMetadataDocument.class);
        query.setParameter("appliesTo", getAppliesToFor(registeredService));
        return query.setMaxResults(1);
    }
}

