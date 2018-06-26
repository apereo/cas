package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningStandaloneCondition;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = {"cas.authn.adaptive.risk.dateTime.enabled=true", "cas.authn.adaptive.risk.dateTime.windowInHours=4"})
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
public class DateTimeAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {

    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        final var authentication = CoreAuthenticationTestUtils.getAuthentication("datetimeperson");
        final var service = RegisteredServiceTestUtils.getRegisteredService("test");
        final var request = new MockHttpServletRequest();
        final var score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        final var authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final var service = RegisteredServiceTestUtils.getRegisteredService("test");
        final var request = new MockHttpServletRequest();
        final var score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isLowestRisk());
    }
}
