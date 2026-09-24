package org.apereo.cas.web.report;

import module java.base;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
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
class SingleSignOnSessionsEndpointTests extends AbstractCasEndpointTests {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    private String username;

    /**
     * Every request below is scoped to this principal, and the session it seeds is removed by
     * principal too, so the report and the removals never reach another test's sessions.
     *
     * @throws Throwable in case of failure
     */
    @BeforeEach
    void setup() throws Throwable {
        username = UUID.randomUUID().toString();
        val result = CoreAuthenticationTestUtils.getAuthenticationResult(CoreAuthenticationTestUtils.getAuthentication(username));
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val st = centralAuthenticationService.grantServiceTicket(tgt.getId(),
            RegisteredServiceTestUtils.getService(), result);
        assertNotNull(st);
    }

    @AfterEach
    public void teardown() throws Exception {
        mockMvc.perform(delete("/actuator/ssoSessions/users/{username}", username)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void verifyDelete() throws Throwable {
        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("username", username)
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

        val authResult = CoreAuthenticationTestUtils.getAuthenticationResult(CoreAuthenticationTestUtils.getAuthentication(username));
        val tgt = centralAuthenticationService.createTicketGrantingTicket(authResult);
        assertNotNull(tgt);

        mockMvc.perform(delete("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("username", username)
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
                .queryParam("username", username)
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
                    .queryParam("username", username)
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
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.ticketGrantingTicket").exists());

        mockMvc.perform(delete("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("username", username)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deleted").exists())
            .andExpect(jsonPath("$.length()", equalTo(1)));
    }

    @Test
    void verifyProxies() throws Throwable {
        val tgt = new MockTicketGrantingTicket(username);
        tgt.setProxiedBy(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()));
        ticketRegistry.addTicket(tgt);

        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType())
                .queryParam("username", username)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeSsoSessions.length()", equalTo(2)));

        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.PROXIED.getType())
                .queryParam("username", username)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeSsoSessions.length()", equalTo(1)));

        mockMvc.perform(get("/actuator/ssoSessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("type", SingleSignOnSessionsEndpoint.SsoSessionReportOptions.DIRECT.getType())
                .queryParam("username", username)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeSsoSessions.length()", equalTo(1)));
    }
}

