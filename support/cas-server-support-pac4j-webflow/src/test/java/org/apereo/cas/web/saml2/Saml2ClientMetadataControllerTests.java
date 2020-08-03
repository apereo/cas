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
 * This is {@link Saml2ClientMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes =
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
)
@Tag("SAML")
public class Saml2ClientMetadataControllerTests {
    @Autowired
    @Qualifier("saml2ClientMetadataController")
    private Saml2ClientMetadataController saml2ClientMetadataController;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    private Action delegatedAuthenticationAction;

    @Test
    public void verifyOperation() {
        assertNotNull(delegatedAuthenticationAction);
        assertNotNull(saml2ClientMetadataController.getFirstIdentityProviderMetadata());
        assertNotNull(saml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertNotNull(saml2ClientMetadataController.getIdentityProviderMetadataByName("SAML2Client"));
        assertNotNull(saml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client"));
    }
}
