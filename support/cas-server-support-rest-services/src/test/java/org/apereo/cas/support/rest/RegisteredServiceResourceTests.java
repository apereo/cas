package org.apereo.cas.support.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link RegisteredServiceResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class RegisteredServiceResourceTests {

    private static final String SERVICE_ID = "serviceId";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private ServicesManager servicesManager;

    @Test
    public void checkRegisteredServiceNotAuthorized() throws Exception {
        configureCasMockToCreateValidTGT();

        final RegisteredServiceResource registeredServiceResource = new RegisteredServiceResource(servicesManager, casMock, "memberOf", "staff");

        configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services/add/TGT-1")
                .param(SERVICE_ID, SERVICE_ID)
                .param(NAME, NAME)
                .param(DESCRIPTION, DESCRIPTION)
                .param("evaluationOrder", "1000")
                .param("enabled", "false")
                .param("ssoEnabled", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNormal() throws Exception {
        configureCasMockToCreateValidTGT();

        final RegisteredServiceResource registeredServiceResource = new RegisteredServiceResource(servicesManager, casMock, "memberOf", "cas");

        configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services/add/TGT-1")
                .param(SERVICE_ID, SERVICE_ID)
                .param(NAME, NAME)
                .param(DESCRIPTION, DESCRIPTION)
                .param("evaluationOrder", "1000")
                .param("enabled", "false")
                .param("ssoEnabled", "true"))
                .andExpect(status().isOk());
    }

    @Test
    public void checkRegisteredServiceNoTgt() throws Exception {
        final RegisteredServiceResource registeredServiceResource = new RegisteredServiceResource(servicesManager, casMock, "memberOf", "staff");

        configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services/add/TGT-1")
                .param(SERVICE_ID, SERVICE_ID)
                .param(NAME, NAME)
                .param(DESCRIPTION, DESCRIPTION)
                .param("evaluationOrder", "1000")
                .param("enabled", "false")
                .param("ssoEnabled", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void checkRegisteredServiceNoAttributeValue() throws Exception {
        final RegisteredServiceResource registeredServiceResource = new RegisteredServiceResource(servicesManager, casMock, "Test", StringUtils.EMPTY);

        configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services/add/TGT-12345"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNoAttributeName() throws Exception {
        final RegisteredServiceResource registeredServiceResource = new RegisteredServiceResource(servicesManager, casMock, null, "staff");

        configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services/add/TGT-12345"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNoAttributes() throws Exception {
        configureMockMvcFor(new RegisteredServiceResource(servicesManager, casMock, null, null))
                .perform(post("/cas/v1/services/add/TGT-12345"))
                .andExpect(status().isBadRequest());
    }

    private void configureCasMockToCreateValidTGT() {
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-1");
        when(tgt.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(
                CoreAuthenticationTestUtils.getPrincipal("casuser",
                        new HashMap<>(RegisteredServiceTestUtils.getTestAttributes()))));
        final Class<TicketGrantingTicket> clazz = TicketGrantingTicket.class;

        when(this.casMock.getTicket(anyString(), any(clazz.getClass()))).thenReturn(tgt);
        when(this.servicesManager.save(any(RegisteredService.class))).thenReturn(
                RegisteredServiceTestUtils.getRegisteredService("TEST"));
    }

    private MockMvc configureMockMvcFor(final RegisteredServiceResource registeredServiceResource) {
        return MockMvcBuilders.standaloneSetup(registeredServiceResource)
                .defaultRequest(get("/")
                        .contextPath("/cas")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .build();
    }
}
