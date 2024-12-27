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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataAmazonS3Tests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(
    classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "CasFeatureModule.DelegatedAuthentication.saml-s3.enabled=true",

        "cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.endpoint=http://127.0.0.1:4566",
        "cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.region=us-east-1",
        "cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.credential-access-key=test",
        "cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.credential-secret-key=test"
    })
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedSaml2ClientMetadataAmazonS3Tests {
    @Autowired
    @Qualifier("delegatedSaml2ClientMetadataController")
    private DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController;

    @Test
    void verifyOperation() {
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertTrue(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());
    }
}
