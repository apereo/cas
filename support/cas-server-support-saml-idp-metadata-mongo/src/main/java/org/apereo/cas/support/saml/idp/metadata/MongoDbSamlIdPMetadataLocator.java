package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
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
    protected void fetchMetadataDocument() {
        setMetadataDocument(mongoTemplate.findOne(new Query(), SamlIdPMetadataDocument.class, this.collectionName));
    }
}
