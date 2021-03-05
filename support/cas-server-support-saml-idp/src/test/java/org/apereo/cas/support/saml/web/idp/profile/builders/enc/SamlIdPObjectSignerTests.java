package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPObjectSignerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
public class SamlIdPObjectSignerTests extends BaseSamlIdPConfigurationTests {

    @Test
    public void findsSigningCredential() {
        val samlRegisteredService = getSamlRegisteredServiceFor(true, true, false, "https://cassp.example.org");
        samlRegisteredService.setId(1000);
        samlRegisteredService.setName("ObjectSignerTest");
        samlRegisteredService.setSigningCredentialFingerprint("4f095b7ce6a7f49112c334a488185d55278177f9");

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, samlRegisteredService,
                samlRegisteredService.getServiceId()).get();

        val authnRequest = getAuthnRequest(samlRegisteredService);
        assertNotNull(
            samlIdPObjectSigner.encode(authnRequest, samlRegisteredService, adaptor, response, request,
                SAMLConstants.SAML2_POST_BINDING_URI, authnRequest));

    }

    private AuthnRequest getAuthnRequest(final SamlRegisteredService samlRegisteredService) {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        var authnRequest = (AuthnRequest) Objects.requireNonNull(builder).buildObject();
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }
}
