package org.apereo.cas.support.saml.metadata.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MongoDbSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {


    private final MongoTemplate mongoTemplate;
    private final String collectionName;

    public MongoDbSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                        final OpenSamlConfigBean configBean, final MongoTemplate mongoTemplate) {
        super(samlIdPProperties, configBean);
        this.collectionName = samlIdPProperties.getMetadata().getMongo().getCollection();
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            final List<SamlMetadataDocument> documents = mongoTemplate.findAll(SamlMetadataDocument.class, this.collectionName);
            return documents
                .stream()
                .map(doc -> buildMetadataResolverFrom(service, doc))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("mongodb://");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void saveOrUpdate(final SamlMetadataDocument document) {
        this.mongoTemplate.save(document, this.collectionName);
    }
}
