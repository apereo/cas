package org.jasig.cas.support.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistrySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Unit tests for {@link org.jasig.cas.support.rest.TicketsResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TicketsResourceTests {

    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private TicketRegistrySupport ticketSupport;

    @InjectMocks
    private TicketsResource ticketsResourceUnderTest;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        final AuthenticationManager mgmr = mock(AuthenticationManager.class);
        when(mgmr.authenticate(any(AuthenticationTransaction.class))).thenReturn(TestUtils.getAuthentication());

        this.ticketsResourceUnderTest.getAuthenticationSystemSupport().getAuthenticationTransactionManager()
                .setAuthenticationManager(mgmr);
        this.ticketsResourceUnderTest.setWebApplicationServiceFactory(new WebApplicationServiceFactory());

        when(this.ticketSupport.getAuthenticationFrom(anyString())).thenReturn(TestUtils.getAuthentication());
        this.ticketsResourceUnderTest.setTicketRegistrySupport(ticketSupport);
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.ticketsResourceUnderTest)
                .defaultRequest(get("/")
                .contextPath("/cas")
                .servletPath("/v1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .build();
    }

    @Test
    public void normalCreationOfTGT() throws Throwable {
        final String expectedReturnEntityBody = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\">"
                + "<html><head><title>201 Created</title></head><body><h1>TGT Created</h1>"
                + "<form action=\"http://localhost/cas/v1/tickets/TGT-1\" "
                + "method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">"
                + "<br><input type=\"submit\" value=\"Submit\"></form></body></html>";

        configureCasMockToCreateValidTGT();

        this.mockMvc.perform(post("/cas/v1/tickets")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void creationOfTGTWithAuthenticationException() throws Throwable {
        configureCasMockTGTCreationToThrowAuthenticationException();

        this.mockMvc.perform(post("/cas/v1/tickets")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"authentication_exceptions\" : [ \"LoginException\" ]}"));
    }

    @Test
    public void creationOfTGTWithUnexpectedRuntimeException() throws Throwable {
        configureCasMockTGTCreationToThrow(new RuntimeException("Other exception"));

        this.mockMvc.perform(post("/cas/v1/tickets")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("Other exception"));
    }

    @Test
    public void creationOfTGTWithBadPayload() throws Throwable {
        configureCasMockTGTCreationToThrow(new RuntimeException("Other exception"));

        this.mockMvc.perform(post("/cas/v1/tickets")
                .param("no_username_param", "test")
                .param("no_password_param", "test"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Invalid payload. 'username' and 'password' form fields are required."));
    }

    @Test
    public void normalCreationOfST() throws Throwable {
        configureCasMockToCreateValidST();

        this.mockMvc.perform(post("/cas/v1/tickets/TGT-1")
                .param("service", TestUtils.getService().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string("ST-1"));
    }

    @Test
    public void creationOfSTWithInvalidTicketException() throws Throwable {
        configureCasMockSTCreationToThrow(new InvalidTicketException("TGT-1"));

        this.mockMvc.perform(post("/cas/v1/tickets/TGT-1")
                .param("service", TestUtils.getService().getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("TicketGrantingTicket could not be found"));
    }

    @Test
    public void creationOfSTWithGeneralException() throws Throwable {
        configureCasMockSTCreationToThrow(new RuntimeException("Other exception"));

        this.mockMvc.perform(post("/cas/v1/tickets/TGT-1")
                .param("service", TestUtils.getService().getId()))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("Other exception"));
    }

    @Test
    public void deletionOfTGT() throws Throwable {
        this.mockMvc.perform(delete("/cas/v1/tickets/TGT-1"))
                .andExpect(status().isOk());
    }

    private void configureCasMockToCreateValidTGT() throws Throwable {
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-1");
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationContext.class))).thenReturn(tgt);

    }

    private void configureCasMockTGTCreationToThrowAuthenticationException() throws Throwable {
        final Map<String, Class<? extends Exception>> handlerErrors = new HashMap<>(1);
        handlerErrors.put("TestCaseAuthenticationHander", LoginException.class);
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationContext.class)))
                .thenThrow(new AuthenticationException(handlerErrors));
    }

    private void configureCasMockTGTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationContext.class))).thenThrow(e);
    }

    private void configureCasMockSTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationContext.class))).thenThrow(e);
    }

    private void configureCasMockToCreateValidST() throws Throwable {
        final ServiceTicket st = mock(ServiceTicket.class);
        when(st.getId()).thenReturn("ST-1");
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationContext.class))).thenReturn(st);
    }
}
