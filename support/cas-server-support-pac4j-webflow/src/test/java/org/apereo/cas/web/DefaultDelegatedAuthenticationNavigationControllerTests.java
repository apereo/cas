package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedAuthenticationNavigationControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultDelegatedAuthenticationNavigationControllerTests {

    @Autowired
    @Qualifier("delegatedClientNavigationController")
    private DefaultDelegatedAuthenticationNavigationController controller;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeEach
    public void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyRedirectByParam() {
        val request = new MockHttpServletRequest();
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient");
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof RedirectView);
    }

    @Test
    public void verifyRedirectByAttr() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof DynamicHtmlView);
    }

    @Test
    public void verifyRedirectByAttrForceAuth() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        request.setParameter(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, "true");
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof DynamicHtmlView);
    }

    @Test
    public void verifyRedirectByAttrPassiveAuth() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        request.setParameter(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, "true");
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof DynamicHtmlView);
    }

    @Test
    public void verifyRedirectWithService() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas"));
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof DynamicHtmlView);
    }

    @Test
    public void verifyRedirectUnknownClient() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "BadClient");
        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class, () -> controller.redirectToProvider(request, response));
    }

    @Test
    public void verifyRedirectMissingClient() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class, () -> controller.redirectToProvider(request, response));
    }

    @Test
    public void redirectResponseToFlow() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://sso.example.org/cas/login/CasClient");
        request.addParameter("param1", "value1");
        val response = new MockHttpServletResponse();
        assertNotNull(controller.redirectResponseToFlow("CasClient", request, response));
        assertNotNull(controller.postResponseToFlow("CasClient", request, response));
    }

    @Test
    public void verifyRedirectWithServiceSaml2Properties() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas"));
        val registeredService = servicesManager.findServiceBy(service);

        val property1 = new DefaultRegisteredServiceProperty("class1", "class2");
        registeredService.getProperties()
            .put(RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_AUTHN_CONTEXT_CLASS_REFS.getPropertyName(), property1);

        val property2 = new DefaultRegisteredServiceProperty("true");
        registeredService.getProperties()
            .put(RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_WANTS_RESPONSES_SIGNED.getPropertyName(), property2);

        servicesManager.save(registeredService);
        
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof DynamicHtmlView);
    }

    @Test
    public void verifyRedirectWithServiceOidcProperties() {
        val request = new MockHttpServletRequest();
        request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "OidcClient");
        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas2");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas2"));

        val registeredService = servicesManager.findServiceBy(service);

        val property1 = new DefaultRegisteredServiceProperty("1000");
        registeredService.getProperties()
            .put(RegisteredServiceProperties.DELEGATED_AUTHN_OIDC_MAX_AGE.getPropertyName(), property1);

        val property2 = new DefaultRegisteredServiceProperty("openid one two three");
        registeredService.getProperties()
            .put(RegisteredServiceProperties.DELEGATED_AUTHN_OIDC_SCOPE.getPropertyName(), property2);

        servicesManager.save(registeredService);

        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        val response = new MockHttpServletResponse();
        assertTrue(controller.redirectToProvider(request, response) instanceof RedirectView);
    }

}
