package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link StatisticsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.statistics.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class StatisticsEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @BeforeEach
    void setup() throws Throwable {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt1 = centralAuthenticationService.createTicketGrantingTicket(result);
        val st1 = centralAuthenticationService.grantServiceTicket(tgt1.getId(),
            CoreAuthenticationTestUtils.getWebApplicationService(), result);
        assertNotNull(st1);

        val tgt2 = centralAuthenticationService.createTicketGrantingTicket(result);
        val st2 = centralAuthenticationService.grantServiceTicket(tgt2.getId(),
            CoreAuthenticationTestUtils.getWebApplicationService(), result);
        assertNotNull(st2);
        tgt2.markTicketExpired();
        st2.markTicketExpired();
    }

    /**
     * Tickets are counted per type, named from the ticket catalog, without removing the expired ones, which the registry
     * cleaner removes; the total, sessions and service tickets add up from the same count, and a second call right
     * after the first answers the same count.
     */
    @Test
    void verifyOperation() throws Throwable {
        val first = mockMvc.perform(get("/actuator/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.upTime").isNumber())
            .andExpect(jsonPath("$.tickets").isNumber())
            .andExpect(jsonPath("$.sessions").isNumber())
            .andExpect(jsonPath("$.serviceTickets").isNumber())
            .andReturn().getResponse().getContentAsString();
        val counted = JsonPath.read(first, "$.counted");
        assertTrue(JsonPath.<Integer>read(first, "$.sessions") >= 2);
        assertTrue(JsonPath.<Integer>read(first, "$.serviceTickets") >= 2);

        val types = JsonPath.<List<Map<String, Object>>>read(first, "$.ticketsByType");
        val total = types.stream().mapToLong(type -> ((Number) type.get("count")).longValue()).sum();
        assertEquals(((Number) JsonPath.read(first, "$.tickets")).longValue(), total);
        val sessions = types.stream().filter(type -> "TGT".equals(type.get("prefix"))).findFirst().orElseThrow();
        assertEquals("Ticket Granting Ticket", sessions.get("name"));
        assertEquals("session", sessions.get("kind"));

        mockMvc.perform(get("/actuator/statistics").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.counted").value(counted));
    }
}

