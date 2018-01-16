package org.apereo.cas.support.saml.metadata.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.ToString;

/**
 * This is {@link JpaSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerSamlMetadata")
@Slf4j
@ToString
public class JpaSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {

    private static final String SELECT_QUERY = "SELECT r from SamlMetadataDocument r ";

    @PersistenceContext(unitName = "samlMetadataEntityManagerFactory")
    private EntityManager entityManager;

    public JpaSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties, final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            final Collection<SamlMetadataDocument> documents = this.entityManager.createQuery(SELECT_QUERY, SamlMetadataDocument.class).getResultList();
            return documents.stream().map(doc -> buildMetadataResolverFrom(service, doc)).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("jdbc://");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void saveOrUpdate(final SamlMetadataDocument document) {
        try {
            this.entityManager.merge(document);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
