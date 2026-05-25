package org.apereo.cas.support.saml.web.idp.profile.sso;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPHttpRedirectDeflateEncoder;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import net.shibboleth.shared.net.URLBuilder;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SSOSamlIdPPostSimpleSignProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
class SSOSamlIdPPostSimpleSignProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    @Order(1)
    void verifyPostSignRequest() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();

        val authnRequest = signAuthnRequest(request, response, getAuthnRequest(), samlRegisteredService);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        val result = performPostSimpleSignRequest(EncodingUtils.encodeBase64(xml));
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    @Order(2)
    void verifyRedirectRequest() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("GET");
        val response = new MockHttpServletResponse();
        val authnRequest = signAuthnRequest(request, response, getAuthnRequest(), samlRegisteredService);

        val encoder = new SamlIdPHttpRedirectDeflateEncoder("https://cas.example.org/login", authnRequest);
        encoder.doEncode();
        val result = performRedirectSimpleSignRequest(encoder.getRedirectUrl());
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    @Order(2)
    void verifyBadRequest() {
        assertDoesNotThrow(() -> {
            val result = performPostSimpleSignRequest("Text");
            assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        });
    }

    private MvcResult performPostSimpleSignRequest(final String samlRequest) throws Exception {
        return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN)
            .param(SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest))
            .andReturn();
    }

    private MvcResult performRedirectSimpleSignRequest(final String redirectUrl) throws Exception {
        val queryStrings = Strings.CI.remove(redirectUrl, "https://cas.example.org/login?");
        val builder = get(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN);
        new URLBuilder(redirectUrl)
            .getQueryParams()
            .forEach(param -> builder.queryParam(param.getFirst(), param.getSecond()));
        builder.with(request -> {
            request.setQueryString(queryStrings);
            return request;
        });
        return mockMvc.perform(builder).andReturn();
    }

    private AuthnRequest getAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        val authnRequest = (AuthnRequest) Objects.requireNonNull(builder).buildObject();
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        return authnRequest;
    }
}
