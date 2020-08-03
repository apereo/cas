package org.apereo.cas.web;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@Tag("Simple")
public class DefaultDelegatedAuthenticationNavigationControllerTests {

    @Autowired
    @Qualifier("delegatedClientNavigationController")
    private DefaultDelegatedAuthenticationNavigationController controller;

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
        request.setRequestURI("https://sso.example.org");
        request.addParameter("param1", "value1");
        val response = new MockHttpServletResponse();
        assertNotNull(controller.redirectResponseToFlow("CasClient", request, response));
        assertNotNull(controller.postResponseToFlow("CasClient", request, response));
    }

}
