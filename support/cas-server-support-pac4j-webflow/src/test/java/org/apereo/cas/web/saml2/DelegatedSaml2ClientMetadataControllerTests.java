package org.apereo.cas.web.saml2;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=/tmp/sp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=src/test/resources/idp-metadata.xml"
    })
@Tag("SAML2Web")
class DelegatedSaml2ClientMetadataControllerTests {
    @Autowired
    @Qualifier("delegatedSaml2ClientMetadataController")
    private DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    private Action delegatedAuthenticationAction;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(delegatedAuthenticationAction);
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstIdentityProviderMetadata());
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertTrue(delegatedSaml2ClientMetadataController.getIdentityProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());
        assertTrue(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());

        assertFalse(delegatedSaml2ClientMetadataController.getIdentityProviderMetadataByName("UnknownClient").getStatusCode().is2xxSuccessful());
        assertFalse(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("UnknownClient").getStatusCode().is2xxSuccessful());
    }
}
