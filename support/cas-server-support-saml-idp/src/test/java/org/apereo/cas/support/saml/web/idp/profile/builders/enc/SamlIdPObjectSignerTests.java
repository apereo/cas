package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPObjectSignerTests}.
 *
 * @author Hayden Sartoris
 * @since 6.3.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.location=classpath:metadata/"
})
public class SamlIdPObjectSignerTests extends BaseSamlIdPConfigurationTests {
    private SamlRegisteredService testService() {
        val service = new SamlRegisteredService();
        service.setName("ObjectSignerTest");
        service.setServiceId("https://cassp.example.org");
        service.setId(1000);
        service.setSigningCredentialFingerprint("4f095b7ce6a7f49112c334a488185d55278177f9");
        service.setSignAssertions(true);
        service.setSignResponses(true);
        service.setEncryptAssertions(false);
        service.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        return service;
    }


    @Test
    public void findsSigningCredential() throws Exception {
        val service = testService();
        samlIdPObjectSigner.getSignatureSigningConfiguration(service);
    }
}
