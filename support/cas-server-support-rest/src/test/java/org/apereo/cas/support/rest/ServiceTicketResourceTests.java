package org.apereo.cas.support.rest;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.support.rest.factory.DefaultServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.support.rest.resources.ServiceTicketResource;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private TicketRegistrySupport ticketSupport;

    @InjectMocks
    private ServiceTicketResource serviceTicketResource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        final AuthenticationManager mgmr = mock(AuthenticationManager.class);
        when(mgmr.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ticketSupport.getAuthenticationFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        this.serviceTicketResource = new ServiceTicketResource(
                new DefaultAuthenticationSystemSupport(new DefaultAuthenticationTransactionManager(mgmr),
                        new DefaultPrincipalElectionStrategy()),
                ticketSupport, new WebApplicationServiceFactory(),
                new DefaultServiceTicketResourceEntityResponseFactory(casMock));

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

    private void configureCasMockSTCreationToThrow(final Throwable e) {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenThrow(e);
    }

    private void configureCasMockToCreateValidST() {
        final ServiceTicket st = mock(ServiceTicket.class);
        when(st.getId()).thenReturn("ST-1");
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationResult.class))).thenReturn(st);
    }
}
