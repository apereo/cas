package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.expiration.builder.TransientSessionTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.oauth.client.TwitterClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationWebApplicationServiceFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class DelegatedAuthenticationWebApplicationServiceFactoryTests {
    private DelegatedAuthenticationWebApplicationServiceFactory factory;

    private TwitterClient twitterClient;

    private DelegatedClientWebflowManager manager;

    private MockHttpServletRequest request;

    private static TransientSessionTicketExpirationPolicyBuilder getExpirationPolicyBuilder() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTst().setTimeToKillInSeconds(60);
        return new TransientSessionTicketExpirationPolicyBuilder(props);
    }

    @BeforeEach
    public void setup() {
        this.request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));

        val cas = mock(CentralAuthenticationService.class);
        val ticket = mock(TransientSessionTicket.class);
        when(ticket.getService()).thenReturn(RegisteredServiceTestUtils.getService("https://example.org"));
        when(cas.getTicket(anyString(), any())).thenReturn(ticket);
        this.manager = new DelegatedClientWebflowManager(cas,
            new DefaultTransientSessionTicketFactory(getExpirationPolicyBuilder()),
            new CasConfigurationProperties(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultArgumentExtractor(new WebApplicationServiceFactory()));

        this.twitterClient = new TwitterClient("key", "secret");
        val clients = new Clients(twitterClient);
        this.factory = new DelegatedAuthenticationWebApplicationServiceFactory(clients, manager, new JEESessionStore());
    }

    @Test
    public void verifyOperation() {
        val id = "https://example.org?" + Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER + '=' + twitterClient.getName();
        assertNotNull(factory.createService(id));
    }

    @Test
    public void verifyCreateByRequestService() {
        val id = "https://example.org?" + Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER + '=' + twitterClient.getName();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        assertNotNull(factory.createService(request));
    }

    @Test
    public void verifyCreateByRequest() {
        val initialReq = new MockHttpServletRequest();
        initialReq.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://example.org");
        val webContext = new JEEContext(initialReq, new MockHttpServletResponse(), new JEESessionStore());
        val ticket = manager.store(webContext, twitterClient);
        request.setRequestURI("https://cas.org");
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, twitterClient.getName());
        request.addParameter(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
        val service = factory.createService(request);
        assertNotNull(service);
        assertEquals("https://example.org", service.getId());
    }
}
