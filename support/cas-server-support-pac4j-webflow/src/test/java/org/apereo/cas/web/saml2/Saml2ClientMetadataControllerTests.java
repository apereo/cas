package org.apereo.cas.web.saml2;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.List;

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
