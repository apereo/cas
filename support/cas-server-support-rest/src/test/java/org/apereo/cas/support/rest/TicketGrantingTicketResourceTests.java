package org.apereo.cas.support.rest;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.logout.DefaultLogoutExecutionPlan;
import org.apereo.cas.logout.DefaultLogoutManager;
import org.apereo.cas.logout.slo.DefaultSingleLogoutRequestExecutor;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.rest.authentication.DefaultRestAuthenticationService;
import org.apereo.cas.rest.factory.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationContextValidationResult;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
public class TicketGrantingTicketResourceTests {

    private static final String TICKETS_RESOURCE_URL = "/cas/v1/tickets";

    private static final String USERNAME = "username";

    private static final String OTHER_EXCEPTION = "Other exception";

    private static final String TEST_VALUE = "test";

    private static final String PASSWORD = "password";

    @Mock
    private MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private TicketRegistrySupport ticketSupport;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private RequestedAuthenticationContextValidator requestedContextValidator;

    @InjectMocks
    private TicketGrantingTicketResource ticketGrantingTicketResourceUnderTest;

    private MockMvc mockMvc;

    @BeforeEach
    public void initialize() {
        val httpRequestCredentialFactory = new UsernamePasswordRestHttpRequestCredentialFactory() {
            @Override
            public List<Credential> fromAuthentication(final HttpServletRequest request,
                                                       final MultiValueMap<String, String> requestBody,
                                                       final Authentication authentication,
                                                       final MultifactorAuthenticationProvider provider) {
                if (provider.getId().contains("unknown")) {
                    return List.of();
                }
                return List.of(new UsernamePasswordCredential("mfa-user", "mfa-user"));
            }
        };

        val publisher = mock(ApplicationEventPublisher.class);
        val manager = mock(AuthenticationManager.class);
        lenient().when(manager.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        lenient().when(ticketSupport.getAuthenticationFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        lenient().when(requestedContextValidator.validateAuthenticationContext(any(), any(), any(), any()))
            .thenReturn(AuthenticationContextValidationResult.builder().success(true).build());
        lenient().when(multifactorTriggerSelectionStrategy.resolve(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        val authenticationSystemSupport = new DefaultAuthenticationSystemSupport(
            new DefaultAuthenticationTransactionManager(publisher, manager),
            new DefaultPrincipalElectionStrategy(), new DefaultAuthenticationResultBuilderFactory(),
            new DefaultAuthenticationTransactionFactory());
        val api = new DefaultRestAuthenticationService(authenticationSystemSupport,
            httpRequestCredentialFactory,
            new WebApplicationServiceFactory(),
            multifactorTriggerSelectionStrategy,
            servicesManager,
            requestedContextValidator);

        val logoutManager = new DefaultLogoutManager(false, new DefaultLogoutExecutionPlan());
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        this.ticketGrantingTicketResourceUnderTest = new TicketGrantingTicketResource(api,
            casMock, new DefaultTicketGrantingTicketResourceEntityResponseFactory(),
            new GenericWebApplicationContext(), new DefaultSingleLogoutRequestExecutor(casMock, logoutManager, applicationContext));

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.ticketGrantingTicketResourceUnderTest)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .build();
    }

    @Test
    public void verifyNormalCreationOfTGT() throws Exception {
        val expectedReturnEntityBody = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\">"
            + "<html><head><title>201 CREATED</title></head><body><h1>TGT Created</h1>"
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
    public void defaultCreationOfTGT() throws Throwable {
        val expectedReturnEntityBody = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\">"
            + "<html><head><title>201 CREATED</title></head><body><h1>TGT Created</h1>"
            + "<form action=\"http://localhost/cas/v1/tickets/TGT-1\" "
            + "method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">"
            + "<br><input type=\"submit\" value=\"Submit\"></form></body></html>";

        configureCasMockToCreateValidTGT();
        mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE)
                .accept(MediaType.ALL))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void verifyHtmlCreationOfTGT() throws Throwable {
        val expectedReturnEntityBody = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\">"
            + "<html><head><title>201 CREATED</title></head><body><h1>TGT Created</h1>"
            + "<form action=\"http://localhost/cas/v1/tickets/TGT-1\" "
            + "method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">"
            + "<br><input type=\"submit\" value=\"Submit\"></form></body></html>";

        configureCasMockToCreateValidTGT();
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE)
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void verifyJsonCreationOfTGT() throws Throwable {
        val expectedReturnEntityBody = "TGT-1";

        configureCasMockToCreateValidTGT();
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void verifyCreateTgtWithMfa() throws Exception {
        when(requestedContextValidator.validateAuthenticationContext(any(), any(), any(), any()))
            .thenReturn(AuthenticationContextValidationResult.builder().success(false).build());
        when(multifactorTriggerSelectionStrategy.resolve(any(), any(), any(), any()))
            .thenReturn(Optional.of(new TestMultifactorAuthenticationProvider()));

        val expectedReturnEntityBody = "TGT-1";
        configureCasMockToCreateValidTGT();
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void creationOfTGTWithAuthenticationException() throws Exception {
        configureCasMockTGTCreationToThrowAuthenticationException();

        val content = this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Login failed"));
    }

    @Test
    public void creationOfTGTWithUnexpectedRuntimeException() throws Exception {
        configureCasMockTGTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param(USERNAME, TEST_VALUE)
                .param(PASSWORD, TEST_VALUE))
            .andExpect(status().is5xxServerError())
            .andExpect(content().string(OTHER_EXCEPTION));
    }

    @Test
    public void creationOfTGTWithBadPayload() throws Exception {
        configureCasMockTGTCreationToThrow(new RuntimeException(OTHER_EXCEPTION));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
                .param("no_username_param", TEST_VALUE)
                .param("no_password_param", TEST_VALUE))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void deletionOfTGT() throws Exception {
        when(this.casMock.getTicket(anyString(), any()))
            .thenReturn(new MockTicketGrantingTicket("casuser"));
        this.mockMvc.perform(delete(TICKETS_RESOURCE_URL + "/TGT-1")).andExpect(status().isOk());
    }

    private void configureCasMockToCreateValidTGT() {
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-1");
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationResult.class))).thenReturn(tgt);
    }

    private void configureCasMockTGTCreationToThrowAuthenticationException() {
        val handlerErrors = new HashMap<String, Throwable>(1);
        handlerErrors.put("TestCaseAuthenticationHandler", new LoginException("Login failed"));
        when(this.casMock.createTicketGrantingTicket(any(AuthenticationResult.class)))
            .thenThrow(new AuthenticationException(handlerErrors));
    }

    private void configureCasMockTGTCreationToThrow(final Exception e) {
        lenient().when(this.casMock.createTicketGrantingTicket(any(AuthenticationResult.class))).thenThrow(e);
    }
}
