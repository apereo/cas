package org.apereo.cas.support.saml.web.idp.profile.sso;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerControllerWithBrowserStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata",
    "cas.authn.saml-idp.core.session-storage-type=BROWSER_STORAGE"
})
class SSOSamlIdPPostProfileHandlerControllerWithBrowserStorageTests extends BaseSamlIdPConfigurationTests {
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyPostSignRequest() throws Exception {
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, getAuthnRequest()).toString();
        val result = performPostProfileRequest(EncodingUtils.encodeBase64(xml));
        val mv = result.getModelAndView();
        assertNotNull(mv);
        assertEquals(CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_WRITE, mv.getViewName());
        assertTrue(mv.getModel().containsKey(BrowserStorage.PARAMETER_BROWSER_STORAGE));
    }

    @Test
    void verifyUnknownBindingLocation() throws Exception {
        val authnRequest = getAuthnRequest();
        authnRequest.setProtocolBinding(SAMLConstants.SAML1_ARTIFACT_BINDING_URI);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        val result = performPostProfileRequest(EncodingUtils.encodeBase64(xml));
        val mv = result.getModelAndView();
        assertNotNull(mv);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, mv.getViewName());
        assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
    }

    private MvcResult performPostProfileRequest(final String samlRequest) throws Exception {
        return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST)
            .param(SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest))
            .andReturn();
    }

    private AuthnRequest getAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        val authnRequest = (AuthnRequest) builder.buildObject();
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        return authnRequest;
    }
}
