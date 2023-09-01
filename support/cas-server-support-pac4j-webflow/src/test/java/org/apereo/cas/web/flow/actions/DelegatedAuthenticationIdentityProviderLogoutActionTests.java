package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationIdentityProviderLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Delegation")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedAuthenticationIdentityProviderLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT)
    private Action action;

    @Autowired
    @Qualifier("delegatedClientAuthenticationConfigurationContext")
    private DelegatedClientAuthenticationConfigurationContext configurationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, action.execute(context).getId());
    }

    @Test
    void verifyPostBackchannelSaml2LogoutOperation() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val saml2MessageContext = new SAML2MessageContext(new CallContext(new JEEContext(request, response),
            configurationContext.getSessionStore()));
        val messageContext = new MessageContext();

        val logoutRequest = mock(LogoutRequest.class);
        val sessionIndex = mock(SessionIndex.class);
        val sessionIdx = UUID.randomUUID().toString();
        when(sessionIndex.getValue()).thenReturn(sessionIdx);
        when(logoutRequest.getSessionIndexes()).thenReturn(List.of(sessionIndex));
        messageContext.setMessage(logoutRequest);
        saml2MessageContext.setMessageContext(messageContext);
        val clientCredentials = new ClientCredential(new SAML2Credentials(saml2MessageContext), "SAML2Client");
        WebUtils.putCredential(context, clientCredentials);

        val tgt = new MockTicketGrantingTicket("casuser",
            clientCredentials, Map.of("sessionindex", List.of(sessionIdx)));
        configurationContext.getTicketRegistry().addTicket(tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_DONE, action.execute(context).getId());
        assertNull(configurationContext.getTicketRegistry().getTicket(tgt.getId()));
    }
}
