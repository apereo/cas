package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MongoDbSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    private final MongoOperations mongoTemplate;
    private final String collectionName;

    public MongoDbSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                        final OpenSamlConfigBean configBean,
                                                        final MongoOperations mongoTemplate) {
        super(samlIdPProperties, configBean);
        this.collectionName = samlIdPProperties.getMetadata().getMongo().getCollection();
        this.mongoTemplate = mongoTemplate;
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.debug("Fetching metadata documents from collection [{}]", this.collectionName);
        val documents = mongoTemplate.findAll(SamlMetadataDocument.class, this.collectionName);
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
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        return mongoTemplate.save(document, this.collectionName);
    }

    @Override
    public void removeById(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        this.mongoTemplate.remove(query, SamlMetadataDocument.class, this.collectionName);
    }

    @Override
    public void removeByName(final String name) {
        val query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        this.mongoTemplate.remove(query, SamlMetadataDocument.class, this.collectionName);
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        val query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        val document = mongoTemplate.findOne(query, SamlMetadataDocument.class, this.collectionName);
        return Optional.ofNullable(document);
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        val document = mongoTemplate.findOne(query, SamlMetadataDocument.class, this.collectionName);
        return Optional.ofNullable(document);
    }

    @Override
    public List<SamlMetadataDocument> load() {
        return mongoTemplate.findAll(SamlMetadataDocument.class, this.collectionName);
    }

    @Override
    public String getSourceId() {
        return "mongodb://";
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service) && mongoTemplate.collectionExists(this.collectionName);
    }
}
