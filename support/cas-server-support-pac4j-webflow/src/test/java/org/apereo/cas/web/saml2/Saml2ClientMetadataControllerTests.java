package org.apereo.cas.web.saml2;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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
public class Saml2ClientMetadataControllerTests {
    @Autowired
    @Qualifier("saml2ClientMetadataController")
    private Saml2ClientMetadataController saml2ClientMetadataController;

    @Test
    public void verifyOperation() {
        assertNotNull(saml2ClientMetadataController.getFirstIdentityProviderMetadata());
        assertNotNull(saml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertNotNull(saml2ClientMetadataController.getIdentityProviderMetadataByName("SAML2Client"));
        assertNotNull(saml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client"));
    }
}
