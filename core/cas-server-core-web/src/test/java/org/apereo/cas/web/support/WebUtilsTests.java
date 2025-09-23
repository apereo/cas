package org.apereo.cas.web.support;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
class WebUtilsTests {

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val flow = new Flow("mockFlow");
        val flowSession = new MockFlowSession(flow);
        flowSession.setParent(new MockFlowSession(flow));
        val mockExecutionContext = new MockFlowExecutionContext(flowSession);
        context.setFlowExecutionContext(mockExecutionContext);

        WebUtils.putLogoutRedirectUrl(context, CoreAuthenticationTestUtils.CONST_TEST_URL);
        assertNotNull(WebUtils.getLogoutRedirectUrl(context, String.class));
        WebUtils.removeLogoutRedirectUrl(context);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));

        assertNull(WebUtils.getHttpServletRequestUserAgentFromRequestContext(context));
        assertNull(WebUtils.getHttpServletRequestUserAgentFromRequestContext(context.getHttpServletRequest()));
        assertNull(WebUtils.getAuthenticationResult(context));
        assertFalse(WebUtils.getHttpServletRequestGeoLocationFromRequestContext().isValid());
        assertNull(WebUtils.getAcceptableUsagePolicyTermsFromFlowScope(context, Object.class));
        assertFalse(WebUtils.hasSurrogateAuthenticationRequest(context));

        assertNotNull(WebUtils.produceUnauthorizedErrorView(new RuntimeException()));
        assertNotNull(WebUtils.produceErrorView(new IllegalArgumentException()));
        assertNotNull(WebUtils.produceErrorView("error-view", new IllegalArgumentException()));
        assertNotNull(WebUtils.getHttpRequestFullUrl(context));

        context.setQueryString("param=value");
        assertNotNull(WebUtils.getHttpRequestFullUrl(context.getHttpServletRequest()));
        assertFalse(WebUtils.isGraphicalUserAuthenticationEnabled(context));
        assertNull(WebUtils.getAvailableAuthenticationHandleNames(context));

        WebUtils.putTargetTransition(context, "example-state");
        assertNotNull(WebUtils.getTargetTransition(context));

        WebUtils.putPasswordManagementQuery(context, null);
        assertNull(WebUtils.getPasswordManagementQuery(context, Serializable.class));

        assertDoesNotThrow(() -> {
            WebUtils.putWildcardedRegisteredService(context, true);
            WebUtils.putInitialHttpRequestPostParameters(context);
            WebUtils.putExistingSingleSignOnSessionAvailable(context, true);
            WebUtils.putExistingSingleSignOnSessionPrincipal(context, CoreAuthenticationTestUtils.getPrincipal());
            WebUtils.putAvailableAuthenticationHandleNames(context, List.of());
            WebUtils.putPasswordManagementEnabled(context, true);
            WebUtils.putForgotUsernameEnabled(context, true);
            WebUtils.putRecaptchaPropertiesFlowScope(context, new GoogleRecaptchaProperties().setEnabled(true));
            WebUtils.putLogoutUrls(context, Map.of());
            WebUtils.putWarnCookieIfRequestParameterPresent(null, context);
            WebUtils.putTicketGrantingTicketInScopes(context, "ticket-id");
        });
        WebUtils.putCredential(context, new UsernamePasswordCredential("casuser", "password"));
        assertThrows(ClassCastException.class, () -> WebUtils.getCredential(context, OneTimeTokenCredential.class));

        WebUtils.putTicketGrantingTicketInScopes(context, StringUtils.EMPTY);
        WebUtils.putTicketGrantingTicketInScopes(context, (Ticket) null);
        WebUtils.putTicketGrantingTicketInScopes(context, (String) null);
        assertNull(WebUtils.getTicketGrantingTicket(context));
        assertThrows(IllegalArgumentException.class, () -> WebUtils.getPrincipalFromRequestContext(context, null));

        context.setParameter(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION, "true");
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        assertTrue(WebUtils.isAuthenticatingAtPublicWorkstation(context));

        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-XYZ123");
        assertNull(WebUtils.getPrincipalFromRequestContext(context, ticketRegistrySupport));

        WebUtils.putLogoutPostUrl(context, CoreAuthenticationTestUtils.CONST_TEST_URL);
        assertEquals(CoreAuthenticationTestUtils.CONST_TEST_URL, WebUtils.getLogoutPostUrl(context));
        val data = new HashMap<String, Object>();
        data.put("SAMLResponse", "xxx");
        WebUtils.putLogoutPostData(context, data);
        assertEquals(data, WebUtils.getLogoutPostData(context));
    }

    @Test
    void verifyFindService() {
        val casArgumentExtractor = new DefaultArgumentExtractor(List.of(RegisteredServiceTestUtils.getWebApplicationServiceFactory()));
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        val service = HttpRequestUtils.getService(List.of(casArgumentExtractor), request);
        assertNotNull(service);
        assertEquals("test", service.getId());
    }

    @Test
    void verifyStorageRead() throws Throwable {
        val context1 = MockRequestContext.create();
        context1.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        context1.setParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE, "test");
        assertTrue(WebUtils.getBrowserStoragePayload(context1).isPresent());
        assertTrue(WebUtils.getRequestParameterOrAttribute(context1, BrowserStorage.PARAMETER_BROWSER_STORAGE).isPresent());

        val context2 = MockRequestContext.create();
        context2.setMethod(HttpMethod.POST);
        context2.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        context2.setContent(BrowserStorage.PARAMETER_BROWSER_STORAGE + '=' + UUID.randomUUID());
        assertTrue(WebUtils.getBrowserStoragePayload(context2).isPresent());
        assertNotNull(context2.getHttpServletRequest().getAttribute(BrowserStorage.PARAMETER_BROWSER_STORAGE));

        val context3 = MockRequestContext.create();
        context3.setMethod(HttpMethod.POST);
        context3.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        assertTrue(WebUtils.getBrowserStoragePayload(context3).isEmpty());
    }

    @Test
    void verifyReadParametersFromRequestBody() throws Throwable {
        val context1 = MockRequestContext.create();
        context1.setMethod(HttpMethod.POST);
        context1.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        context1.setContent(BrowserStorage.PARAMETER_BROWSER_STORAGE + '=' + UUID.randomUUID());
        var parameters = WebUtils.getHttpRequestParametersFromRequestBody(context1.getHttpServletRequest());
        assertTrue(parameters.containsKey(BrowserStorage.PARAMETER_BROWSER_STORAGE));
        assertNotNull(context1.getHttpServletRequest().getAttribute(BrowserStorage.PARAMETER_BROWSER_STORAGE));
        assertTrue(WebUtils.getRequestParameterOrAttribute(context1, BrowserStorage.PARAMETER_BROWSER_STORAGE).isPresent());

        parameters = WebUtils.getHttpRequestParametersFromRequestBody(context1.getHttpServletRequest());
        assertTrue(parameters.isEmpty());
    }

    @Test
    void verifyErrorViewWithRootCause() {
        val view = WebUtils.produceErrorView(new RuntimeException(new AuthenticationException("error")));
        val error = (Exception) view.getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION);
        assertInstanceOf(AuthenticationException.class, error);
    }
}
