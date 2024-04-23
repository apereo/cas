package org.apereo.cas.web.saml2;

import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataMongoDbTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasDelegatedAuthenticationAutoConfiguration.class,
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
},
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        
        "CasFeatureModule.DelegatedAuthentication.saml-mongodb.enabled=true",
        
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.host=localhost",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.port=27017",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.drop-collection=true",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.user-id=root",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.password=secret",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.database-name=saml2",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.collection=spmetadata",
        "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.authentication-database-name=admin",

        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=src/test/resources/idp-metadata.xml"
    })
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedSaml2ClientMetadataMongoDbTests {
    @Autowired
    @Qualifier("delegatedSaml2ClientMetadataController")
    private DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController;


    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertTrue(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());
    }
}
