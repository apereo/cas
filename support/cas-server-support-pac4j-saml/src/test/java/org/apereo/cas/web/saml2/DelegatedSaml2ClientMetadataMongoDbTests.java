package org.apereo.cas.web.saml2;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataMongoDbTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class, properties = {
    "CasFeatureModule.DelegatedAuthentication.saml-mongodb.enabled=true",

    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.host=localhost",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.port=27017",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.drop-collection=true",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.user-id=root",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.password=secret",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.database-name=saml2",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.collection=spmetadata",
    "cas.authn.pac4j.saml[0].metadata.service-provider.mongo.authentication-database-name=admin"
})
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedSaml2ClientMetadataMongoDbTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/metadata"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/SAML2Client/metadata"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
    }
}
