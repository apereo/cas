package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlSubjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
public class SamlProfileSamlSubjectBuilderTests extends BaseSamlIdPConfigurationTests {

    @Test
    public void verifySubjectWithNoNameId() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSkipGeneratingAssertionNameId(true);
        service.setSkipGeneratingSubjectConfirmationNotOnOrAfter(true);
        service.setSkipGeneratingSubjectConfirmationNameId(true);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val result = samlProfileSamlSubjectBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(result);
    }
}
