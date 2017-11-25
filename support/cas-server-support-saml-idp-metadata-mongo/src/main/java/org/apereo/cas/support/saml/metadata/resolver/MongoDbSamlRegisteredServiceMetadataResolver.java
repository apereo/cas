package org.apereo.cas.support.saml.metadata.resolver;

import net.shibboleth.ext.spring.resource.ResourceHelper;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.ResourceUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.ResourceBackedMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
public class MongoDbSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbSamlRegisteredServiceMetadataResolver.class);

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
            final List<MongoDbMetadata> documents = mongoTemplate.findAll(MongoDbMetadata.class, this.collectionName);
            return documents
                .stream()
                .map(doc -> buildMongoDbMetadataResolver(service, doc))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    private AbstractMetadataResolver buildMongoDbMetadataResolver(final SamlRegisteredService service,
                                                                  final MongoDbMetadata document) {
        try {
            final StringReader reader = new StringReader(document.getValue());
            final InputStream is = new ReaderInputStream(reader, StandardCharsets.UTF_8);
            final String desc = StringUtils.defaultString(service.getDescription(), service.getName());
            final InputStreamResource metadataResource = new InputStreamResource(is, desc);
            final AbstractMetadataResolver metadataResolver = new InMemoryResourceMetadataResolver(ResourceHelper.of(metadataResource));
            buildSingleMetadataResolver(metadataResolver, service);
            return metadataResolver;
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
}
