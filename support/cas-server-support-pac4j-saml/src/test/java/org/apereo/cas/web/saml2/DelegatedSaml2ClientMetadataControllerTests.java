package org.apereo.cas.web.saml2;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("SAML2Web")
class DelegatedSaml2ClientMetadataControllerTests {
    @Autowired
    @Qualifier("delegatedSaml2ClientMetadataController")
    private DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    private Action delegatedAuthenticationAction;

    @Test
    void verifyOperation() {
        assertNotNull(delegatedAuthenticationAction);
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstIdentityProviderMetadata());
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertTrue(delegatedSaml2ClientMetadataController.getIdentityProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());
        assertTrue(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());

        assertFalse(delegatedSaml2ClientMetadataController.getIdentityProviderMetadataByName("UnknownClient").getStatusCode().is2xxSuccessful());
        assertFalse(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("UnknownClient").getStatusCode().is2xxSuccessful());
    }
}
