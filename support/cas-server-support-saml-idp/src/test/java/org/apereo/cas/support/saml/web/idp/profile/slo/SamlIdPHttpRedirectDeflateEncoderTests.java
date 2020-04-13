package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPHttpRedirectDeflateEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SamlIdPHttpRedirectDeflateEncoderTests extends BaseSamlIdPConfigurationTests {
    @Test
    @Order(1)
    public void verify() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
        servicesManager.save(service);
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
        var logoutRequest = (LogoutRequest) builder.buildObject();

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(service.getServiceId());
        logoutRequest.setIssuer(issuer);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        logoutRequest = samlIdPObjectSigner.encode(logoutRequest, service,
            adaptor, response, request, SAMLConstants.SAML2_REDIRECT_BINDING_URI, logoutRequest);

        val encoder = new SamlIdPHttpRedirectDeflateEncoder("https://cas.example.org/logout", logoutRequest);
        encoder.doEncode();
        assertNotNull(encoder.getRedirectUrl());
        assertNotNull(encoder.getMessageContext());
        assertNotNull(encoder.getEncodedRequest());
    }
}
