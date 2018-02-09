package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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
@TestPropertySource(properties = "cas.authn.adaptive.risk.dateTime.enabled=true")
public class DateTimeAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {

    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("datetimeperson");
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("test");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("test");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isLowestRisk());
    }
}
