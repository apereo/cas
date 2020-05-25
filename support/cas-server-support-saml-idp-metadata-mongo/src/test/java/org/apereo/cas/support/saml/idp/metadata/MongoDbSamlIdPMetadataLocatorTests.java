package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseMongoDbSamlMetadataTests;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.mongo.database-name=saml-idp-generator",
    "cas.authn.saml-idp.metadata.mongo.host=localhost",
    "cas.authn.saml-idp.metadata.mongo.port=27017",
    "cas.authn.saml-idp.metadata.mongo.user-id=root",
    "cas.authn.saml-idp.metadata.mongo.password=secret",
    "cas.authn.saml-idp.metadata.mongo.authentication-database-name=admin",
    "cas.authn.saml-idp.metadata.mongo.drop-collection=true",
    "cas.authn.saml-idp.metadata.mongo.idp-metadata-collection=saml-idp-metadata"
})
@Tag("MongoDb")
@EnabledIfPortOpen(port = 27017)
public class MongoDbSamlIdPMetadataLocatorTests extends BaseMongoDbSamlMetadataTests {

    @Test
    public void verifySigningKeyWithoutService() {
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
