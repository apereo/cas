package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DelegatedClientWebflowManager;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.JEEContext;
import org.pac4j.oauth.client.OAuth10Client;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth10Configuration;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientWebflowManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes =
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Webflow")
public class DelegatedClientWebflowManagerTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("delegatedClientWebflowManager")
    private DelegatedClientWebflowManager delegatedClientWebflowManager;

    private JEEContext context;

    private MockRequestContext requestContext;

    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    public void setup() {
        val service = RegisteredServiceTestUtils.getService();
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context = new JEEContext(httpServletRequest, new MockHttpServletResponse());

        requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(),
            context.getNativeRequest(), context.getNativeResponse()));
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(requestContext.getExternalContext());
    }

    @Test
    public void verifyOidcStoreOperation() {
        val config = new OidcConfiguration();
        config.setClientId(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OidcClient<>(config);
        val ticket = delegatedClientWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertTrue(config.isWithState());
        assertEquals(ticket.getId(), config.getStateGenerator().generateValue(context));

        httpServletRequest.addParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER, ticket.getId());
        val service = delegatedClientWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyOAuth2StoreOperation() {
        val config = new OAuth20Configuration();
        config.setKey(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OAuth20Client();
        client.setConfiguration(config);
        val ticket = delegatedClientWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertTrue(config.isWithState());
        assertEquals(ticket.getId(), config.getStateGenerator().generateValue(context));

        assertThrows(UnauthorizedServiceException.class,
            () -> delegatedClientWebflowManager.retrieve(requestContext, context, client));

        httpServletRequest.addParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER, ticket.getId());
        val service = delegatedClientWebflowManager.retrieve(requestContext, context, client);

        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyOAuth1StoreOperation() {
        val config = new OAuth10Configuration();
        config.setKey(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OAuth10Client();
        client.setConfiguration(config);
        val ticket = delegatedClientWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        val service = delegatedClientWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyCasStoreOperation() {
        val config = new CasConfiguration();
        config.setLoginUrl("https://example.org/login");
        val client = new CasClient();
        client.setConfiguration(config);
        val ticket = delegatedClientWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), config.getCustomParams().get(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID));
        val service = delegatedClientWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifySamlStoreOperation() {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        val ticket = delegatedClientWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), context.getSessionStore().get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());

        httpServletRequest.addParameter("RelayState", ticket.getId());
        val service = delegatedClientWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyExpiredTicketOperation() {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        val ticket = delegatedClientWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), context.getSessionStore().get(context,
            SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());
        httpServletRequest.addParameter("RelayState", ticket.getId());
        ticket.markTicketExpired();
        assertThrows(UnauthorizedServiceException.class,
            () -> delegatedClientWebflowManager.retrieve(requestContext, context, client));
    }

}
