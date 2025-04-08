package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedAuthenticationClientsTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Action;
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
@SpringBootTest(classes = {
    DelegatedAuthenticationClientsTestConfiguration.class,
    BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class
})
@ExtendWith(CasTestExtension.class)
class DelegatedAuthenticationIdentityProviderLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT)
    private Action action;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
    private DelegatedClientAuthenticationConfigurationContext configurationContext;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        context.withUserAgent();
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, action.execute(context).getId());
    }

    @Test
    void verifyPostLogout() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent();
        context.setMethod(HttpMethod.POST);
        val tgt = prepCredential(context, UUID.randomUUID().toString(), "AutomaticPostLogoutClient");
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "AutomaticPostLogoutClient");
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val continuation = SingleLogoutContinuation.builder().url("http://localhost:%s".formatted(webServer.getPort()));
            context.setRequestAttribute(SingleLogoutContinuation.class.getName(), continuation.build());
            assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, action.execute(context).getId());
            assertNotNull(configurationContext.getTicketRegistry().getTicket(tgt.getId()));
            assertNull(context.getHttpServletRequest().getAttribute(SingleLogoutContinuation.class.getName()));
        }
    }

    @Test
    void verifyPostBackChannelSaml2LogoutOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setMethod(HttpMethod.POST);
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        context.withUserAgent();
        val tgt = prepCredential(context, UUID.randomUUID().toString(), "SAML2Client");
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, action.execute(context).getId());
        assertNotNull(configurationContext.getTicketRegistry().getTicket(tgt.getId()));
    }

    private Ticket prepCredential(final MockRequestContext context, final String principal, final String clientName) throws Exception {
        val sessionIdx = UUID.randomUUID().toString();
        val clientCredentials = getClientCredential(context, sessionIdx, clientName);
        val tgt = new MockTicketGrantingTicket(principal, clientCredentials, Map.of("sessionindex", List.of(sessionIdx)));
        configurationContext.getTicketRegistry().addTicket(tgt);
        WebUtils.putCredential(context, clientCredentials);
        return tgt;
    }

    private ClientCredential getClientCredential(final MockRequestContext context, final String sessionIdx,
                                                 final String clientName) {
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val saml2MessageContext = new SAML2MessageContext(new CallContext(webContext, configurationContext.getSessionStore()));
        val messageContext = new MessageContext();
        val logoutRequest = mock(LogoutRequest.class);
        val sessionIndex = mock(SessionIndex.class);
        when(sessionIndex.getValue()).thenReturn(sessionIdx);
        when(logoutRequest.getSessionIndexes()).thenReturn(List.of(sessionIndex));
        messageContext.setMessage(logoutRequest);
        saml2MessageContext.setMessageContext(messageContext);
        return new ClientCredential(new SAML2Credentials(saml2MessageContext), clientName);
    }
}
