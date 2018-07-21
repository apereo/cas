package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseMongoDbSamlMetadataTests;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(locations = {"classpath:/samlidp-mongo.properties"})
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
