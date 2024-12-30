package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UserAgentAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = "cas.authn.adaptive.risk.agent.enabled=true")
@Tag("Authentication")
class UserAgentAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {

    @Test
    void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("nobody1");
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
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)");

        request.setRemoteAddr("107.181.69.221");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));
        assertTrue(score.isRiskGreaterThan(casProperties.getAuthn().getAdaptive().getRisk().getCore().getThreshold()));
    }
}
