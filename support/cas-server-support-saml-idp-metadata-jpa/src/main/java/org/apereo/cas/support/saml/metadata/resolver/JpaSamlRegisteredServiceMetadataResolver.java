package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private static final int DATA_SOURCE_VALIDITY_TIMEOUT_SECONDS = 5;

    private static final String SELECT_QUERY = String.format("SELECT r from %s r ", SamlMetadataDocument.class.getSimpleName());

    @PersistenceContext(unitName = "samlMetadataEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties, final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try {
            val documents = this.entityManager.createQuery(SELECT_QUERY, SamlMetadataDocument.class).getResultList();
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
            val metadataLocation = service.getMetadataLocation();
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

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val ds = JpaBeans.newDataSource(samlIdPProperties.getMetadata().getJpa());
            try (val con = ds.getConnection()) {
                return con.isValid(DATA_SOURCE_VALIDITY_TIMEOUT_SECONDS);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return false;
    }
}
