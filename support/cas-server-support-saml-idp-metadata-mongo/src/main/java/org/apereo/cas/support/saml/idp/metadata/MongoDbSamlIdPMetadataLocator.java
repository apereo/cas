package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Monitorable
public class MongoDbSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final MongoOperations mongoTemplate;

    private final String collectionName;

    public MongoDbSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                         final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                         final MongoOperations mongoTemplate,
                                         final ConfigurableApplicationContext applicationContext,
                                         final String collectionName) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val document = findDocumentFor(registeredService);
            if (document != null && document.isValid()) {
                return document;
            }
        }
        return findDocumentFor(Optional.empty());
    }

    /**
     * Finds the document belonging to the given owner.
     * <p>
     * Per-service and global documents share one collection, distinguished only by {@code appliesTo},
     * so an unscoped query is not "the global document" -- it is whichever document the collection
     * happens to return first, which can be a service's own document and, through it, that service's
     * signing and encryption keys. The global owner has a name of its own, so it is queried by name
     * like any other.
     *
     * @param registeredService the registered service, or empty for the global document
     * @return the document, or null when the owner has none
     */
    private SamlIdPMetadataDocument findDocumentFor(final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = getAppliesToFor(registeredService);
        val query = new Query().addCriteria(Criteria.where("appliesTo").is(appliesTo));
        LOGGER.trace("Fetching SAML IdP metadata document for [{}] from [{}]", appliesTo, this.collectionName);
        return mongoTemplate.findOne(query, SamlIdPMetadataDocument.class, this.collectionName);
    }
}
