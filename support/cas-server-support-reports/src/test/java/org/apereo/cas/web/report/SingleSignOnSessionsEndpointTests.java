package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SingleSignOnSessionsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.ssoSessions.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Execution(ExecutionMode.SAME_THREAD)
class SingleSignOnSessionsEndpointTests extends AbstractCasEndpointTests {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @BeforeEach
    void setup() throws Throwable {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val st = centralAuthenticationService.grantServiceTicket(tgt.getId(),
            RegisteredServiceTestUtils.getService(), result);
        assertNotNull(st);
    }

    @AfterEach
    public void teardown() throws Exception {
        mockMvc.perform(delete("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType())
            )
            .andExpect(status().isOk());
    }

    @Test
    void verifyDelete() throws Throwable {
        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("username", CoreAuthenticationTestUtils.CONST_USERNAME)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(delete("/actuator/ssoSessions/unknown-ticket")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.ticketGrantingTicket").exists());

        val authResult = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt = centralAuthenticationService.createTicketGrantingTicket(authResult);
        assertNotNull(tgt);

        mockMvc.perform(delete("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));
        assertTrue(ticketRegistry.getTickets(ticket -> ticket.getId().equals(tgt.getId()) && !ticket.isExpired()).findAny().isEmpty());
    }

    @Test
    void verifyDeleteByUser() throws Throwable {
        mockMvc.perform(delete("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("username", CoreAuthenticationTestUtils.CONST_USERNAME)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$..status").exists())
            .andExpect(jsonPath("$..ticketGrantingTicket").exists());
    }


    @Test
    void verifyOperation() throws Exception {
        val sessions = (List) MAPPER.readValue(mockMvc.perform(get("/actuator/ssoSessions")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$.activeSsoSessions").exists())
                .andExpect(jsonPath("$.activeSsoSessions.length()", equalTo(1)))
                .andReturn()
                .getResponse()
                .getContentAsString(), Map.class)
            .get("activeSsoSessions");

        val tgt = ((Map) sessions.getFirst()).get(SingleSignOnSessionsEndpoint.SsoSessionAttributeKeys.TICKET_GRANTING_TICKET_ID.getAttributeKey()).toString();

        mockMvc.perform(delete("/actuator/ssoSessions/" + tgt)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.ticketGrantingTicket").exists());

        mockMvc.perform(delete("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.length()", equalTo(1)));
    }

    @Test
    void verifyProxies() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setProxiedBy(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()));
        ticketRegistry.addTicket(tgt);

        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", equalTo(2)));

        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.PROXIED.getType())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeSsoSessions.length()", equalTo(1)));

        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.DIRECT.getType())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeSsoSessions.length()", equalTo(1)));
    }
}

