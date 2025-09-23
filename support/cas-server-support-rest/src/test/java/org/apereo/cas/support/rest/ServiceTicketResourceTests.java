package org.apereo.cas.support.rest;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.rest.factory.CasProtocolServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.resources.ServiceTicketResource;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link TicketGrantingTicketResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("RestfulApi")
class ServiceTicketResourceTests {

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
    void initialize() throws Throwable {
        val mgmr = mock(AuthenticationManager.class);
        lenient().when(mgmr.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        lenient().when(ticketSupport.getAuthenticationFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        this.serviceTicketResource = new ServiceTicketResource(
            CoreAuthenticationTestUtils.getAuthenticationSystemSupport(mgmr, mock(ServicesManager.class)),
            ticketSupport,
            new DefaultArgumentExtractor(List.of(RegisteredServiceTestUtils.getWebApplicationServiceFactory())),
            new CasProtocolServiceTicketResourceEntityResponseFactory(casMock),
            new UsernamePasswordRestHttpRequestCredentialFactory(),
            new GenericApplicationContext());

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.serviceTicketResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .accept(MediaType.APPLICATION_FORM_URLENCODED, MediaType.TEXT_PLAIN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .build();
    }

    @Test
    void normalCreationOfST() throws Throwable {
        configureCasMockToCreateValidST();

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/x-www-form-urlencoded;charset=ISO-8859-1"))
            .andExpect(content().string("ST-1"));
    }

    @Test
    void normalCreationOfSTWithRenew() throws Throwable {
        configureCasMockToCreateValidST();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId())
            .param(RENEW, "true")
            .param(USERNAME, TEST_VALUE)
            .param(PASSWORD, TEST_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/x-www-form-urlencoded;charset=ISO-8859-1"))
            .andExpect(content().string("ST-1"))
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("ST-1"));
    }

    @Test
    void creationOfSTWithInvalidTicketException() throws Throwable {
        configureCasMockSTCreationToThrow(new InvalidTicketException("TGT-1"));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void creationOfSTWithGeneralException() throws Throwable {
        configureCasMockSTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
            .andExpect(status().is5xxServerError())
            .andExpect(content().string(OTHER_EXCEPTION));
    }

    @Test
    void creationOfSTWithBadRequestException() throws Throwable {
        configureCasMockToCreateValidST();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId())
            .param(RENEW, "true"))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("No credentials"));
    }

    @Test
    void creationOfSTWithAuthenticationException() throws Throwable {
        configureCasMockSTCreationToThrowAuthenticationException();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
            .param(SERVICE, CoreAuthenticationTestUtils.getService().getId())
            .param(RENEW, "true")
            .param(USERNAME, TEST_VALUE)
            .param(PASSWORD, TEST_VALUE))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Login failed"));
    }

    private void configureCasMockSTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenThrow(e);
    }

    private void configureCasMockToCreateValidST() throws Throwable {
        val st = mock(ServiceTicket.class);
        lenient().when(st.getId()).thenReturn("ST-1");
        lenient().when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenReturn(st);
    }

    private void configureCasMockSTCreationToThrowAuthenticationException() throws Throwable {
        val handlerErrors = new HashMap<String, Throwable>(1);
        handlerErrors.put("TestCaseAuthenticationHandler", new LoginException("Login failed"));
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class)))
            .thenThrow(new AuthenticationException(handlerErrors));
    }
}
