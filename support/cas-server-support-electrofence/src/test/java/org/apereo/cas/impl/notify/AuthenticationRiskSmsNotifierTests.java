package org.apereo.cas.impl.notify;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.impl.calcs.BaseAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthenticationRiskSmsNotifierTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.adaptive.risk.ip.enabled=true",

    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.authn.adaptive.risk.response.mail.from=cas@example.org",
    "cas.authn.adaptive.risk.response.mail.text=Message",
    "cas.authn.adaptive.risk.response.mail.subject=Subject",

    "cas.authn.adaptive.risk.response.sms.text=Message",
    "cas.authn.adaptive.risk.response.sms.from=3487244312"
})
@Tag("SMS")
class AuthenticationRiskSmsNotifierTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @BeforeEach
    void onSetUp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.100");
        request.setLocalAddr("223.456.789.200");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOperation() {
        authenticationRiskSmsNotifier.setClientInfo(ClientInfoHolder.getClientInfo());
        authenticationRiskSmsNotifier.setRegisteredService(CoreAuthenticationTestUtils.getRegisteredService());
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("phone", List.of("3487244312")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        authenticationRiskSmsNotifier.setAuthentication(authentication);
        authenticationRiskSmsNotifier.setAuthenticationRiskScore(AuthenticationRiskScore.highestRiskScore());
        assertDoesNotThrow(() -> authenticationRiskSmsNotifier.publish());
    }
}
