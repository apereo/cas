package org.apereo.cas.support.rest;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.rest.factory.CasProtocolServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.support.rest.resources.ServiceTicketResource;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link TicketGrantingTicketResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceTicketResourceTests {

    private static final String TICKETS_RESOURCE_URL = "/cas/v1/tickets";
    private static final String OTHER_EXCEPTION = "Other exception";
    private static final String SERVICE = "service";
    private static final String RENEW = "renew";
    private static final String TEST_VALUE = "test";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private TicketRegistrySupport ticketSupport;

    @InjectMocks
    private ServiceTicketResource serviceTicketResource;

    private MockMvc mockMvc;

    @BeforeEach
    public void initialize() {
        val mgmr = mock(AuthenticationManager.class);
        when(mgmr.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ticketSupport.getAuthenticationFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        val publisher = mock(ApplicationEventPublisher.class);

        this.serviceTicketResource = new ServiceTicketResource(
            new DefaultAuthenticationSystemSupport(new DefaultAuthenticationTransactionManager(publisher, mgmr),
                new DefaultPrincipalElectionStrategy()),
            ticketSupport, new DefaultArgumentExtractor(new WebApplicationServiceFactory()),
            new CasProtocolServiceTicketResourceEntityResponseFactory(casMock),
            new UsernamePasswordRestHttpRequestCredentialFactory(),
            new GenericApplicationContext());

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.serviceTicketResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .build();
    }

    @Test
    public void normalCreationOfST() throws Exception {
        configureCasMockToCreateValidST();

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string("ST-1"));
    }

    @Test
    public void normalCreationOfSTWithRenew() throws Exception {
        configureCasMockToCreateValidST();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId())
            .param(RENEW, "true")
            .param(USERNAME, TEST_VALUE)
            .param(PASSWORD, TEST_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string("ST-1"))
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("ST-1"));
    }

    @Test
    public void creationOfSTWithInvalidTicketException() throws Exception {
        configureCasMockSTCreationToThrow(new InvalidTicketException("TGT-1"));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    public void creationOfSTWithGeneralException() throws Exception {
        configureCasMockSTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
            .andExpect(status().is5xxServerError())
            .andExpect(content().string(OTHER_EXCEPTION));
    }

    @Test
    public void creationOfSTWithBadRequestException() throws Exception {
        configureCasMockToCreateValidST();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId())
            .param(RENEW, "true"))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("No credentials"));
    }

    @Test
    public void creationOfSTWithAuthenticationException() throws Exception {
        configureCasMockSTCreationToThrowAuthenticationException();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId())
            .param(RENEW, "true")
            .param(USERNAME, TEST_VALUE)
            .param(PASSWORD, TEST_VALUE))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("LoginException"));
    }

    private void configureCasMockSTCreationToThrow(final Throwable e) {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenThrow(e);
    }

    private void configureCasMockToCreateValidST() {
        val st = mock(ServiceTicket.class);
        when(st.getId()).thenReturn("ST-1");
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenReturn(st);
    }

    private void configureCasMockSTCreationToThrowAuthenticationException() {
        val handlerErrors = new HashMap<String, Throwable>(1);
        handlerErrors.put("TestCaseAuthenticationHandler", new LoginException("Login failed"));
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class)))
            .thenThrow(new AuthenticationException(handlerErrors));
    }
}
