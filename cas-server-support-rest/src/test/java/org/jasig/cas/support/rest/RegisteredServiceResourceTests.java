package org.jasig.cas.support.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;

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
public class RegisteredServiceResourceTests {

    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private ServicesManager servicesManager;

    @InjectMocks
    private RegisteredServiceResource registeredServiceResource;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.registeredServiceResource)
                .defaultRequest(get("/")
                .contextPath("/cas")
                .servletPath("/v1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .build();
    }

    @Test
    public void checkRegisteredServiceNotAuthorized() throws Exception {
        configureCasMockToCreateValidTGT();


        this.registeredServiceResource.setAttributeName("memberOf");
        this.registeredServiceResource.setAttributeValue("staff");
        this.mockMvc.perform(post("/cas/v1/services/add/TGT-1")
            .param("serviceId", "serviceId")
            .param("name", "name")
            .param("description", "description")
            .param("evaluationOrder", "1000")
            .param("enabled", "false")
            .param("ssoEnabled", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNormal() throws Exception {
        configureCasMockToCreateValidTGT();


        this.registeredServiceResource.setAttributeName("memberOf");
        this.registeredServiceResource.setAttributeValue("cas");
        this.mockMvc.perform(post("/cas/v1/services/add/TGT-1")
            .param("serviceId", "serviceId")
            .param("name", "name")
            .param("description", "description")
            .param("evaluationOrder", "1000")
            .param("enabled", "false")
            .param("ssoEnabled", "true"))
            .andExpect(status().isOk());
    }

    @Test
    public void checkRegisteredServiceNoTgt() throws Exception {

        this.registeredServiceResource.setAttributeName("memberOf");
        this.registeredServiceResource.setAttributeValue("staff");
        this.mockMvc.perform(post("/cas/v1/services/add/TGT-1")
                    .param("serviceId", "serviceId")
                    .param("name", "name")
                    .param("description", "description")
                    .param("evaluationOrder", "1000")
                    .param("enabled", "false")
                    .param("ssoEnabled", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void checkRegisteredServiceNoAttributeValue() throws Exception {
        this.registeredServiceResource.setAttributeName("Test");
        this.registeredServiceResource.setAttributeValue("");
        this.mockMvc.perform(post("/cas/v1/services/add/TGT-12345"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNoAttributeName() throws Exception {
        this.registeredServiceResource.setAttributeValue("Test");
        this.mockMvc.perform(post("/cas/v1/services/add/TGT-12345"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNoAttributes() throws Exception {
        this.mockMvc.perform(post("/cas/v1/services/add/TGT-12345"))
            .andExpect(status().isBadRequest());
    }



    private void configureCasMockToCreateValidTGT() throws Exception {
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-1");
        when(tgt.getAuthentication()).thenReturn(org.jasig.cas.authentication.TestUtils.getAuthentication(
                org.jasig.cas.authentication.TestUtils.getPrincipal("casuser",
                        new HashMap(TestUtils.getTestAttributes()))));
        final Class<TicketGrantingTicket> clazz = TicketGrantingTicket.class;

        when(this.casMock.getTicket(anyString(), any(clazz.getClass()))).thenReturn(tgt);
        when(this.servicesManager.save(any(RegisteredService.class))).thenReturn(
                org.jasig.cas.services.TestUtils.getRegisteredService("TEST"));
    }
}
