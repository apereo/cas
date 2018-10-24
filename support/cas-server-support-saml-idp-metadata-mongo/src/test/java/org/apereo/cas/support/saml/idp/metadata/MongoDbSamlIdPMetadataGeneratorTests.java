package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseMongoDbSamlMetadataTests;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.samlIdp.metadata.mongo.databaseName=saml-idp-generator",
    "cas.authn.samlIdp.metadata.mongo.host=localhost",
    "cas.authn.samlIdp.metadata.mongo.port=27017",
    "cas.authn.samlIdp.metadata.mongo.userId=root",
    "cas.authn.samlIdp.metadata.mongo.password=secret",
    "cas.authn.samlIdp.metadata.mongo.authenticationDatabaseName=admin",
    "cas.authn.samlIdp.metadata.mongo.dropCollection=true",
    "cas.authn.samlIdp.metadata.mongo.idpMetadataCollection=saml-idp-metadata"
    })
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 27017)
public class MongoDbSamlIdPMetadataGeneratorTests extends BaseMongoDbSamlMetadataTests {
    @Test
    public void verifyOperation() {
        this.samlIdPMetadataGenerator.generate();
        assertNotNull(samlIdPMetadataLocator.getMetadata());
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate());
        assertNotNull(samlIdPMetadataLocator.getEncryptionKey());
        assertNotNull(samlIdPMetadataLocator.getSigningCertificate());
        assertNotNull(samlIdPMetadataLocator.getSigningKey());
    }
}
