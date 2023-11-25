package org.apereo.cas.web.support;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        context.getHttpServletRequest().setQueryString("param=value");
        assertNotNull(WebUtils.getHttpRequestFullUrl(context.getHttpServletRequest()));
        assertFalse(WebUtils.isGraphicalUserAuthenticationEnabled(context));
        assertNull(WebUtils.getAvailableAuthenticationHandleNames(context));

        WebUtils.putTargetTransition(context, "example-state");
        assertNotNull(WebUtils.getTargetTransition(context));

        WebUtils.putPasswordManagementQuery(context, null);
        assertNull(WebUtils.getPasswordManagementQuery(context, Serializable.class));

        assertDoesNotThrow(() -> {
            WebUtils.putWildcardedRegisteredService(context, true);
            WebUtils.putYubiKeyMultipleDeviceRegistrationEnabled(context, true);
            WebUtils.putInitialHttpRequestPostParameters(context);
            WebUtils.putExistingSingleSignOnSessionAvailable(context, true);
            WebUtils.putExistingSingleSignOnSessionPrincipal(context, CoreAuthenticationTestUtils.getPrincipal());
            WebUtils.putAvailableAuthenticationHandleNames(context, List.of());
            WebUtils.putPasswordManagementEnabled(context, true);
            WebUtils.putRecaptchaPropertiesFlowScope(context, new GoogleRecaptchaProperties().setEnabled(true));
            WebUtils.putLogoutUrls(context, Map.of());
            val ac = OneTimeTokenAccount.builder()
                .validationCode(123456)
                .username("casuser")
                .name("Example")
                .build();
            WebUtils.putOneTimeTokenAccount(context, ac);
            assertNotNull(WebUtils.getOneTimeTokenAccount(context, OneTimeTokenAccount.class));
            WebUtils.putOneTimeTokenAccounts(context, List.of(ac));

            WebUtils.putWarnCookieIfRequestParameterPresent(null, context);
            WebUtils.putTicketGrantingTicketInScopes(context, "ticket-id");
        });
        WebUtils.putCredential(context, new UsernamePasswordCredential("casuser", "password"));
        assertThrows(ClassCastException.class, () -> WebUtils.getCredential(context, OneTimeTokenCredential.class));

        WebUtils.putTicketGrantingTicketInScopes(context, StringUtils.EMPTY);
        WebUtils.putTicketGrantingTicketInScopes(context, (TicketGrantingTicket) null);
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
    void verifyFindService() throws Throwable {
        val casArgumentExtractor = new DefaultArgumentExtractor(new WebApplicationServiceFactory());
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        val service = HttpRequestUtils.getService(List.of(casArgumentExtractor), request);
        assertNotNull(service);
        assertEquals("test", service.getId());
    }
    
}
