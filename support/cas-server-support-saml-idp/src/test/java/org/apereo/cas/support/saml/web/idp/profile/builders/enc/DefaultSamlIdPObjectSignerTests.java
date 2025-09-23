package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSamlIdPObjectSignerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML2")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
class DefaultSamlIdPObjectSignerTests extends BaseSamlIdPConfigurationTests {

    @Test
    void findsSigningCredential() throws Exception {
        val samlRegisteredService = getSamlRegisteredServiceFor(true, true, false, "https://cassp.example.org");
        samlRegisteredService.setId(1000);
        samlRegisteredService.setName("ObjectSignerTest");
        samlRegisteredService.setSigningCredentialFingerprint("4f095b7ce6a7f49112c334a488185d55278177f9");

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, samlRegisteredService,
                samlRegisteredService.getServiceId()).get();
        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, samlRegisteredService);
        val encodedRequest = samlIdPObjectSigner.encode(authnRequest, samlRegisteredService, adaptor, response, request,
            SAMLConstants.SAML2_POST_BINDING_URI, authnRequest, new MessageContext());
        assertNotNull(encodedRequest);

    }
}
