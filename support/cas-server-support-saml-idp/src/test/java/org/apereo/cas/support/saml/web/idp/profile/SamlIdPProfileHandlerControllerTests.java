package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPProfileCallbackHandlerControllerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@Import(SSOSamlIdPProfileCallbackHandlerControllerTests.SamlIdPTestConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.authentication-context-class-mappings=context1->mfa-dummy",
    "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata"
})
public class SamlIdPProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ssoPostProfileHandlerController")
    private SSOSamlIdPPostProfileHandlerController controller;

    @Test
    public void verifyContextMapping() {
        val classRef = mock(AuthnContextClassRef.class);
        when(classRef.getURI()).thenReturn("context1");
        val request = new MockHttpServletRequest();
        val authnRequest = getAuthnRequestFor(UUID.randomUUID().toString());
        val requestedContext = mock(RequestedAuthnContext.class);
        when(requestedContext.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
        when(authnRequest.getRequestedAuthnContext()).thenReturn(requestedContext);
        val results = controller.buildRedirectUrlByRequestedAuthnContext("https://google.com", authnRequest, request);
        assertTrue(results.contains("mfa-dummy"));
    }
    
    @Test
    public void verifyNoMetadataForRequest() throws Exception {
        val service = new SamlRegisteredService();
        service.setServiceId(UUID.randomUUID().toString());
        servicesManager.save(service);
        
        val request = new MockHttpServletRequest();
        val authnRequest = getAuthnRequestFor(service.getServiceId());

        val context = Pair.of(authnRequest, new MessageContext());
        assertThrows(UnauthorizedServiceException.class,
            () -> controller.verifySamlAuthenticationRequest(context, request));
    }

    @Test
    public void verifyNoSignAuthnRequest() throws Exception {
        val service = new SamlRegisteredService();
        service.setServiceId(UUID.randomUUID().toString());
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        val authnRequest = getAuthnRequestFor(service.getServiceId());

        val adaptor = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(adaptor.isAuthnRequestsSigned()).thenReturn(true);
        val context = new MessageContext();
        context.setMessage(authnRequest);
        assertThrows(SAMLException.class,
            () -> controller.verifyAuthenticationContextSignature(context, request, authnRequest, adaptor, service));
    }


    @Test
    public void verifyException() {
        val request = new MockHttpServletRequest();
        request.addParameter("username", "casuser");
        val results = controller.handleUnauthorizedServiceException(request, new IllegalStateException());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, results.getViewName());
        assertTrue(results.getModel().containsKey("rootCauseException"));
        
        assertThrows(UnauthorizedServiceException.class,
            () -> controller.verifySamlRegisteredService(StringUtils.EMPTY));
    }
}
