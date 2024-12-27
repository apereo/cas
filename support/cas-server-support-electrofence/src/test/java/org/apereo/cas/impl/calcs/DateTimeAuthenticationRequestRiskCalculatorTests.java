package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.adaptive.risk.date-time.enabled=true",
    "cas.authn.adaptive.risk.date-time.window-in-hours=4"
})
@Tag("Authentication")
class DateTimeAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @Test
    void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("datetimeperson");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));
        assertTrue(score.isHighestRisk());
    }

    @Test
    void verifyTestWhenAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));
        assertTrue(score.isLowestRisk());
    }

}
