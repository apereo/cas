package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StatisticsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.statistics.enabled=true")
@Tag("ActuatorEndpoint")
public class StatisticsEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("statisticsReportEndpoint")
    private StatisticsEndpoint statisticsEndpoint;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @BeforeEach
    public void setup() {
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
    public void verifyOperation() {
        val results = statisticsEndpoint.handle();
        assertFalse(results.isEmpty());
    }
}

