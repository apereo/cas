package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link UserAgentAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = "cas.authn.adaptive.risk.agent.enabled=true")
public class UserAgentAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {

    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        final var authentication = CoreAuthenticationTestUtils.getAuthentication("nobody1");
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("test");
        final var request = new MockHttpServletRequest();
        final var score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        final var authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("test");
        final var request = new MockHttpServletRequest();
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)");

        request.setRemoteAddr("107.181.69.221");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        final var score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isRiskGreaterThan(casProperties.getAuthn().getAdaptive().getRisk().getThreshold()));
    }
}
