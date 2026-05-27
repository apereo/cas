package org.apereo.cas.support.saml.web.idp.profile;

import module java.base;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.Assertions;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SamlIdPInitiatedProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
class SamlIdPInitiatedProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        this.samlRegisteredService = getSamlRegisteredServiceForTestShib();
        this.samlRegisteredService.setSignUnsolicitedAuthnRequest(true);
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifySignedAuthnRequest() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.setServiceId("signed:authn:service");
        servicesManager.save(service);

        val result = performIdPInitiatedSsoRequest(
            SamlIdPConstants.PROVIDER_ID, service.getServiceId(),
            SamlIdPConstants.TARGET, "relay-state");
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyNoShire() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setServiceId("no:acs:service");
        servicesManager.save(service);

        Assertions.assertThrowsWithRootCause(Exception.class, MessageDecodingException.class,
            () -> performIdPInitiatedSsoRequest(SamlIdPConstants.PROVIDER_ID, service.getServiceId()));
    }

    @Test
    void verifyBadServiceWithNoMetadata() throws Throwable {
        val service = new SamlRegisteredService();
        service.setServiceId(UUID.randomUUID().toString());
        service.setName(service.getServiceId());
        servicesManager.save(service);

        val result = performIdPInitiatedSsoRequest(SamlIdPConstants.PROVIDER_ID, service.getServiceId());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
        assertInstanceOf(UnauthorizedServiceException.class,
            result.getModelAndView().getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }

    @Test
    void verifyNoProvider() {
        Assertions.assertThrowsWithRootCause(Exception.class, MessageDecodingException.class,
            this::performIdPInitiatedSsoRequest);
    }


    @Test
    void verifyBadService() throws Throwable {
        val result = performIdPInitiatedSsoRequest(SamlIdPConstants.PROVIDER_ID, "xxxxxx");
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
        assertInstanceOf(UnauthorizedServiceException.class,
            result.getModelAndView().getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }

    @Test
    void verifyOperation() throws Throwable {
        val result = performIdPInitiatedSsoRequest(
            SamlIdPConstants.PROVIDER_ID, samlRegisteredService.getServiceId(),
            "CName1", "SomeParameter",
            "CName2", "SomeParameter",
            SamlIdPConstants.SIGNATURE, "some-signature",
            SamlProtocolConstants.PARAMETER_SAML_REQUEST, "some-saml-request",
            SamlIdPConstants.TARGET, "relay-state");
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        val redirectUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("CName1="));
        assertTrue(redirectUrl.contains("CName2="));
        assertFalse(redirectUrl.contains("%s=".formatted(SamlProtocolConstants.PARAMETER_SAML_REQUEST)));
        assertFalse(redirectUrl.contains("%s=".formatted(SamlIdPConstants.SIGNATURE)));
    }

    @Test
    void verifyOperationWithTime() throws Throwable {
        val result = performIdPInitiatedSsoRequest(
            SamlIdPConstants.PROVIDER_ID, samlRegisteredService.getServiceId(),
            SamlIdPConstants.TARGET, "relay-state",
            SamlIdPConstants.TIME, String.valueOf(new Date().getTime()));
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
    }

    private MvcResult performIdPInitiatedSsoRequest(final String... parameters) throws Exception {
        val builder = get(SamlIdPConstants.ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO);
        for (var i = 0; i < parameters.length; i += 2) {
            builder.param(parameters[i], parameters[i + 1]);
        }
        return mockMvc.perform(builder).andReturn();
    }
}
