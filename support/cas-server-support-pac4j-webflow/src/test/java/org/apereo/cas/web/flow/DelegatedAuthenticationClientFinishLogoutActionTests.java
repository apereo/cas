package org.apereo.cas.web.flow;

import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.api.SAML2ProfileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationClientFinishLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
public class DelegatedAuthenticationClientFinishLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT)
    private Action delegatedAuthenticationClientFinishLogoutAction;

    @Autowired
    @Qualifier("builtClients")
    private Clients builtClients;

    @Test
    public void verifyOperationWithRedirect() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putDelegatedAuthenticationClientName(context, "SAML2Client");
        WebUtils.putLogoutRedirectUrl(context, "https://google.com");
        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
        val samlClient = (SAML2Client) builtClients.findClient("SAML2Client").get();
        assertEquals("https://google.com", samlClient.getLogoutValidator().getPostLogoutURL());
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));
    }

    @Test
    public void verifyOperationWithRelay() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, "SAML2Client");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val samlClient = (SAML2Client) builtClients.findClient("SAML2Client").get();
        val handler = mock(SAML2ProfileHandler.class);
        when(handler.receive(any())).thenThrow(new IllegalArgumentException());
        samlClient.setLogoutProfileHandler(handler);

        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
    }

    @Test
    public void verifyOperationFailsWithError() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, "SAML2Client");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val samlClient = (SAML2Client) builtClients.findClient("SAML2Client").get();
        val handler = mock(SAML2ProfileHandler.class);
        when(handler.receive(any())).thenThrow(new FoundAction("https://google.com"));
        samlClient.setLogoutProfileHandler(handler);
        
        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        assertEquals("https://google.com", response.getHeader("Location"));
    }
}
