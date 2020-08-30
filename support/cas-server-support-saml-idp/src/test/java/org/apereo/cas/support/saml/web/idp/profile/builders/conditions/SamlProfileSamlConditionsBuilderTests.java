package org.apereo.cas.support.saml.web.idp.profile.builders.conditions;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

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
 * This is {@link SamlProfileSamlConditionsBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.response.skew-allowance=0",
    "cas.saml-core.skew-allowance=3000"
})
public class SamlProfileSamlConditionsBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyWithSkew() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSkewAllowance(5000);
        service.setAssertionAudiences("https://www.example.com");
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val result = samlProfileSamlConditionsBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(result);
    }
}
