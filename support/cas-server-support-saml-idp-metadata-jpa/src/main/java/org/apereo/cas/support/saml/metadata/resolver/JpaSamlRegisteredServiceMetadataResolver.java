package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.LoggingUtils;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * This is {@link JpaSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerSamlMetadata")
@Slf4j
@ToString
public class JpaSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    private static final int DATA_SOURCE_VALIDITY_TIMEOUT_SECONDS = 5;

    private static final String SELECT_QUERY = String.format("SELECT r from %s r ", SamlMetadataDocument.class.getSimpleName());

    @PersistenceContext(unitName = "jpaSamlMetadataContext")
    private EntityManager entityManager;

    public JpaSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties, final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val documents = this.entityManager.createQuery(SELECT_QUERY, SamlMetadataDocument.class).getResultList();
        return documents
            .stream()
            .map(doc -> buildMetadataResolverFrom(service, doc))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith(getSourceId());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public String getSourceId() {
        return "jdbc://";
    }

    @Override
    public List<SamlMetadataDocument> load() {
        return this.entityManager.createQuery(SELECT_QUERY, SamlMetadataDocument.class).getResultList();
    }

    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        return this.entityManager.merge(document);
    }

    @Override
    public void removeById(final long id) {
        findById(id).ifPresent(this.entityManager::remove);
    }

    @Override
    public void removeByName(final String name) {
        findByName(name).ifPresent(this.entityManager::remove);
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        val query = SELECT_QUERY + "WHERE r.name = :name";
        return this.entityManager.createQuery(query, SamlMetadataDocument.class)
            .setParameter("name", name)
            .getResultStream()
            .findFirst();
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        return Optional.ofNullable(this.entityManager.find(SamlMetadataDocument.class, id));
    }

    @Override
    public void removeAll() {
        val query = String.format("DELETE FROM %s", SamlMetadataDocument.class.getSimpleName());
        this.entityManager.createQuery(query).executeUpdate();
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val ds = JpaBeans.newDataSource(samlIdPProperties.getMetadata().getJpa());
            return JpaBeans.isValidDataSourceConnection(ds, DATA_SOURCE_VALIDITY_TIMEOUT_SECONDS);
        }
        return false;
    }
    
}
