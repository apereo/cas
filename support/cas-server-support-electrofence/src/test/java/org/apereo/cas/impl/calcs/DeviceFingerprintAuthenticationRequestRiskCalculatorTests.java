package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeviceFingerprintAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = "cas.authn.adaptive.risk.device-fingerprint.enabled=true")
@Tag("Authentication")
class DeviceFingerprintAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @Test
    void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        request.setParameter("deviceFingerprint", UUID.randomUUID().toString());
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));
        assertTrue(score.isHighestRisk());
    }
}
