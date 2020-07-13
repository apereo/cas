package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

/**
 * This is {@link MongoDbSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class MongoDbSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final transient MongoTemplate mongoTemplate;

    private final String collectionName;

    public MongoDbSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                         final MongoTemplate mongoTemplate,
                                         final String collectionName) {
        super(metadataCipherExecutor);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val query = new Query();
            val appliesTo = getAppliesToFor(registeredService);
            query.addCriteria(Criteria.where("appliesTo").is(appliesTo));
            LOGGER.trace("Fetching SAML IdP metadata document for [{}] from [{}]", appliesTo, this.collectionName);
            val document = mongoTemplate.findOne(query, SamlIdPMetadataDocument.class, this.collectionName);
            if (document != null && document.isValid()) {
                LOGGER.trace("Found SAML IdP metadata document [{}] for [{}] from [{}]", document, appliesTo, this.collectionName);
                return document;
            }
        }
        LOGGER.trace("Fetching SAML IdP metadata document from [{}]", this.collectionName);
        return mongoTemplate.findOne(new Query(), SamlIdPMetadataDocument.class, this.collectionName);
    }

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }
}
