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
@Tag("Simple")
public class StatisticsEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("statisticsReportEndpoint")
    private StatisticsEndpoint statisticsEndpoint;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @BeforeEach
    public void setup() {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val st = centralAuthenticationService.grantServiceTicket(tgt.getId(),
            CoreAuthenticationTestUtils.getService(), result);
        assertNotNull(st);
    }

    @Test
    public void verifyOperation() {
        val results = statisticsEndpoint.handle();
        assertFalse(results.isEmpty());
    }
}

