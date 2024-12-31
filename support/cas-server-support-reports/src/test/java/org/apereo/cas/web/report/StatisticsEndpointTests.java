package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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

    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(get("/actuator/statistics")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}

