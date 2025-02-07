package org.apereo.cas.web.saml2;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedSaml2ClientLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.session-replication.replicate-sessions=false")
@Tag("Delegation")
class DelegatedSaml2ClientLogoutActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
    private Action delegatedSaml2ClientLogoutAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private SessionStore delegatedClientDistributedSessionStore;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory ticketFactory;

    @Test
    void verifyOperationPostMethod() throws Exception {
        val sessionIndexValue = UUID.randomUUID().toString();
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString(),
            Map.of("sessionindex", List.of(sessionIndexValue)));
        val ticket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
            authentication, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);

        val context = MockRequestContext.create(applicationContext);
        context.setMethod(HttpMethod.POST);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val manager = new ProfileManager(webContext, delegatedClientDistributedSessionStore);
        val profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        profile.setClientName("SAML2Client");
        manager.save(true, profile, false);

        val saml2MessageContext = new SAML2MessageContext(new CallContext(webContext, delegatedClientDistributedSessionStore));
        val messageContext = new MessageContext();
        val logoutRequest = mock(LogoutRequest.class);
        val sessionIndex = mock(SessionIndex.class);
        when(sessionIndex.getValue()).thenReturn(sessionIndexValue);
        when(logoutRequest.getSessionIndexes()).thenReturn(List.of(sessionIndex));
        messageContext.setMessage(logoutRequest);
        saml2MessageContext.setMessageContext(messageContext);
        val clientCred = new ClientCredential(new SAML2Credentials(saml2MessageContext), profile.getClientName(), false, profile);
        WebUtils.putCredential(context, clientCred);
        delegatedSaml2ClientLogoutAction.execute(context);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    void verifyOperationLogoutRequestParameter() throws Exception {
        val sessionIndexValue = UUID.randomUUID().toString();
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString(),
                Map.of("sessionindex", List.of(sessionIndexValue)));
        val ticket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                authentication, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);

        val context = MockRequestContext.create(applicationContext);
        context.setMethod(HttpMethod.GET);
        context.setParameter(CasProtocolConstants.PARAMETER_LOGOUT_REQUEST, "adirectlogoutrequesttotreat");
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val manager = new ProfileManager(webContext, delegatedClientDistributedSessionStore);
        val profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        profile.setClientName("SAML2Client");
        manager.save(true, profile, false);

        val saml2MessageContext = new SAML2MessageContext(new CallContext(webContext, delegatedClientDistributedSessionStore));
        val messageContext = new MessageContext();
        val logoutRequest = mock(LogoutRequest.class);
        val sessionIndex = mock(SessionIndex.class);
        when(sessionIndex.getValue()).thenReturn(sessionIndexValue);
        when(logoutRequest.getSessionIndexes()).thenReturn(List.of(sessionIndex));
        messageContext.setMessage(logoutRequest);
        saml2MessageContext.setMessageContext(messageContext);
        val clientCred = new ClientCredential(new SAML2Credentials(saml2MessageContext), profile.getClientName(), false, profile);
        WebUtils.putCredential(context, clientCred);
        delegatedSaml2ClientLogoutAction.execute(context);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    void verifyLogoutResponse() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.setMethod(HttpMethod.POST);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val manager = new ProfileManager(webContext, delegatedClientDistributedSessionStore);
        val profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        profile.setClientName("SAML2Client");
        manager.save(true, profile, false);

        var delegatedClientLogoutRequest = DelegatedAuthenticationClientLogoutRequest
            .builder()
            .target("https://google.com")
            .status(200)
            .build();

        val logoutRequestId = UUID.randomUUID().toString();
        val logoutRequestTicketId = TransientSessionTicketFactory.normalizeTicketId(logoutRequestId);
        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val transientSessionTicket = transientFactory.create(logoutRequestTicketId,
            Map.of(DelegatedAuthenticationClientLogoutRequest.class.getName(), delegatedClientLogoutRequest));
        ticketRegistry.addTicket(transientSessionTicket);
        val saml2MessageContext = new SAML2MessageContext(new CallContext(webContext, delegatedClientDistributedSessionStore));
        val messageContext = new MessageContext();
        val logoutResponse = mock(LogoutResponse.class);
        when(logoutResponse.getInResponseTo()).thenReturn(logoutRequestId);
        messageContext.setMessage(logoutResponse);
        saml2MessageContext.setMessageContext(messageContext);
        val clientCred = new ClientCredential(new SAML2Credentials(saml2MessageContext),
            profile.getClientName(), false, profile);
        WebUtils.putCredential(context, clientCred);
        delegatedSaml2ClientLogoutAction.execute(context);
        delegatedClientLogoutRequest = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(context,
            DelegatedAuthenticationClientLogoutRequest.class);
        assertNotNull(delegatedClientLogoutRequest);
        assertNull(DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequestTicket(context));
        assertNull(ticketRegistry.getTicket(logoutRequestTicketId));
    }
}
