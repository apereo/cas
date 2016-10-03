package org.jasig.cas.support.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.ticket.InvalidTicketException;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Unit tests for {@link TicketsResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TicketsResourceServiceTicketTests {

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
                .servletPath("/v1/tickets/TGT-1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .setUseSuffixPatternMatch(true)
                .build();
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
    
    private void configureCasMockSTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class), any(AuthenticationContext.class))).thenThrow(e);
    }

}
