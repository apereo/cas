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
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
 * Unit tests for {@link TicketsResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TicketsResourceTests {

    private static final String TICKETS_RESOURCE_URL = "/cas/v1/tickets";
    private static final String USERNAME = "username";
    private static final String OTHER_EXCEPTION = "Other exception";
    private static final String SERVICE = "service";
    private static final String TEST_VALUE = "test";
    private static final String PASSWORD = "password";
    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private TicketRegistrySupport ticketSupport;

    @InjectMocks
    private TicketsResource ticketsResourceUnderTest;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        final AuthenticationManager mgmr = mock(AuthenticationManager.class);
        when(mgmr.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ticketSupport.getAuthenticationFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        this.ticketsResourceUnderTest = new TicketsResource(
                new DefaultAuthenticationSystemSupport(new DefaultAuthenticationTransactionManager(mgmr),
                new DefaultPrincipalElectionStrategy()), new DefaultCredentialFactory(), 
                ticketSupport, new WebApplicationServiceFactory(), casMock);

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.ticketsResourceUnderTest)
                .defaultRequest(get("/")
                        .contextPath("/cas")
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

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void creationOfTGTWithAuthenticationException() throws Throwable {
        configureCasMockTGTCreationToThrowAuthenticationException();

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"authentication_exceptions\" : [ \"LoginException\" ]}"));
    }

    @Test
    public void creationOfTGTWithUnexpectedRuntimeException() throws Throwable {
        configureCasMockTGTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(OTHER_EXCEPTION));
    }

    @Test
    public void creationOfTGTWithBadPayload() throws Throwable {
        configureCasMockTGTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param("no_username_param", TEST_VALUE)
                .param("no_password_param", TEST_VALUE))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Invalid payload. 'username' and 'password' form fields are required."));
    }

    @Test
    public void normalCreationOfST() throws Throwable {
        configureCasMockToCreateValidST();

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
                .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string("ST-1"));
    }

    @Test
    public void creationOfSTWithInvalidTicketException() throws Throwable {
        configureCasMockSTCreationToThrow(new InvalidTicketException("TGT-1"));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
                .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("TicketGrantingTicket could not be found"));
    }

    @Test
    public void creationOfSTWithGeneralException() throws Throwable {
        configureCasMockSTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL + "/TGT-1")
                .param(SERVICE, CoreAuthenticationTestUtils.getService().getId()))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(OTHER_EXCEPTION));
    }

    @Test
    public void deletionOfTGT() throws Throwable {
        this.mockMvc.perform(delete(TICKETS_RESOURCE_URL + "/TGT-1"))
                .andExpect(status().isOk());
    }

    private void configureCasMockToCreateValidTGT() throws Throwable {
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-1");
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationResult.class))).thenReturn(tgt);

    }

    private void configureCasMockTGTCreationToThrowAuthenticationException() throws Throwable {
        final Map<String, Class<? extends Exception>> handlerErrors = new HashMap<>(1);
        handlerErrors.put("TestCaseAuthenticationHander", LoginException.class);
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationResult.class)))
                .thenThrow(new AuthenticationException(handlerErrors));
    }

    private void configureCasMockTGTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationResult.class))).thenThrow(e);
    }

    private void configureCasMockSTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenThrow(e);
    }

    private void configureCasMockToCreateValidST() throws Throwable {
        final ServiceTicket st = mock(ServiceTicket.class);
        when(st.getId()).thenReturn("ST-1");
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenReturn(st);
    }
}
