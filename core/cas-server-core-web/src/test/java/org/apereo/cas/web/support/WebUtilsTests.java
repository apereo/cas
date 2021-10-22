package org.apereo.cas.web.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

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
public class WebUtilsTests {

    private static final String URL = "https://logout.com";

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val flow = new Flow("mockFlow");
        val flowSession = new MockFlowSession(flow);
        flowSession.setParent(new MockFlowSession(flow));
        val mockExecutionContext = new MockFlowExecutionContext(flowSession);
        context.setFlowExecutionContext(mockExecutionContext);

        WebUtils.putLogoutRedirectUrl(context, URL);
        assertNotNull(WebUtils.getLogoutRedirectUrl(context, String.class));
        WebUtils.removeLogoutRedirectUrl(context);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));

        assertNull(WebUtils.getHttpServletRequestUserAgentFromRequestContext(context));
        assertNull(WebUtils.getHttpServletRequestUserAgentFromRequestContext(request));
        assertNull(WebUtils.getAuthenticationResult(context));
        assertNull(WebUtils.getHttpServletRequestGeoLocationFromRequestContext());
        assertNull(WebUtils.getAcceptableUsagePolicyTermsFromFlowScope(context, Object.class));
        assertFalse(WebUtils.hasSurrogateAuthenticationRequest(context));

        assertNotNull(WebUtils.produceUnauthorizedErrorView(new RuntimeException()));
        assertNotNull(WebUtils.produceErrorView(new IllegalArgumentException()));
        assertNotNull(WebUtils.produceErrorView("error-view", new IllegalArgumentException()));
        assertNotNull(WebUtils.getHttpRequestFullUrl(context));
        
        request.setQueryString("param=value");
        assertNotNull(WebUtils.getHttpRequestFullUrl(request));
        assertFalse(WebUtils.isGraphicalUserAuthenticationEnabled(context));
        assertTrue(WebUtils.getDelegatedAuthenticationProviderConfigurations(context).isEmpty());
        assertNull(WebUtils.getAvailableAuthenticationHandleNames(context));
        

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
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
            }
        });
        WebUtils.putCredential(context, new UsernamePasswordCredential("casuser", "password"));
        assertThrows(ClassCastException.class, () -> WebUtils.getCredential(context, OneTimeTokenCredential.class));

        WebUtils.putTicketGrantingTicketInScopes(context, StringUtils.EMPTY);
        WebUtils.putTicketGrantingTicketInScopes(context, (TicketGrantingTicket) null);
        WebUtils.putTicketGrantingTicketInScopes(context, (String) null);
        assertNull(WebUtils.getTicketGrantingTicket(context));
        assertThrows(IllegalArgumentException.class, () -> WebUtils.getPrincipalFromRequestContext(context, null));

        request.addParameter(WebUtils.PUBLIC_WORKSTATION_ATTRIBUTE, "true");
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        assertTrue(WebUtils.isAuthenticatingAtPublicWorkstation(context));

        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-XYZ123");
        assertNull(WebUtils.getPrincipalFromRequestContext(context, ticketRegistrySupport));

        WebUtils.putLogoutPostUrl(context, URL);
        assertEquals(URL, WebUtils.getLogoutPostUrl(context));
        val data = new HashMap<String, Object>();
        data.put("SAMLResponse", "xxx");
        WebUtils.putLogoutPostData(context, data);
        assertEquals(data, WebUtils.getLogoutPostData(context));
    }
}
