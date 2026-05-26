package org.apereo.cas.support.saml.web.idp.profile;

import module java.base;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SamlIdPProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
class SamlIdPProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Test
    void verifyNoMetadataForRequest() throws Exception {
        val service = new SamlRegisteredService();
        service.setServiceId(UUID.randomUUID().toString());
        service.setName("SAML2Service");
        servicesManager.save(service);

        val result = performPostProfileRequest(getAuthnRequestFor(service.getServiceId()));
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
        assertInstanceOf(UnauthorizedServiceException.class,
            result.getModelAndView().getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }

    @Test
    void verifyNoSignAuthnRequest() throws Exception {
        val service = new SamlRegisteredService();
        service.setName("SignedSaml2Service");
        service.setServiceId("https://bard.zoom.us");
        service.setMetadataLocation("classpath:metadata/sp-metadata-multicerts.xml");
        servicesManager.save(service);

        val result = performPostProfileRequest(getAuthnRequestFor(service.getServiceId()));
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
        assertInstanceOf(SAMLException.class,
            result.getModelAndView().getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }
    
    @Test
    void verifyException() throws Exception {
        val authnRequest = getAuthnRequestFor(" ");
        val result = performPostProfileRequest(authnRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
        assertTrue(result.getModelAndView().getModel().containsKey(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
        assertInstanceOf(UnauthorizedServiceException.class,
            result.getModelAndView().getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }

    private MvcResult performPostProfileRequest(final AuthnRequest authnRequest) throws Exception {
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml)))
            .andReturn();
    }
}
