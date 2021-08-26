package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BrowserSessionStorage;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerControllerWithBrowserStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata",
    "cas.authn.saml-idp.core.session-storage-type=BROWSER_SESSION_STORAGE"
})
public class SSOSamlIdPPostProfileHandlerControllerWithBrowserStorageTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ssoPostProfileHandlerController")
    private SSOSamlIdPPostProfileHandlerController controller;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    public void verifyPostSignRequest() throws Exception {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, getAuthnRequest()).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        val mv = controller.handleSaml2ProfileSsoPostRequest(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_SESSION_STORAGE_WRITE, mv.getViewName());
        assertTrue(mv.getModel().containsKey(BrowserSessionStorage.KEY_SESSION_STORAGE));
    }

    @Test
    public void verifyUnknownBindingLocation() throws Exception {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();
        val authnRequest = getAuthnRequest();
        authnRequest.setProtocolBinding(SAMLConstants.SAML1_ARTIFACT_BINDING_URI);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        val mv = controller.handleSaml2ProfileSsoPostRequest(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, mv.getViewName());
        assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
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
        return authnRequest;
    }
}
