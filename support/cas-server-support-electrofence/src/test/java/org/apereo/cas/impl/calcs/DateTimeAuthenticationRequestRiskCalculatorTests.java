package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
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
    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("datetimeperson");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isLowestRisk());
    }
}
