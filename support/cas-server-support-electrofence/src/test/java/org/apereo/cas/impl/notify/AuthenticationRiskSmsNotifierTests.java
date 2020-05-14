package org.apereo.cas.impl.notify;

import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.impl.calcs.BaseAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

/**
 * This is {@link AuthenticationRiskSmsNotifierTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.authn.adaptive.risk.response.mail.from=cas@example.org",
    "cas.authn.adaptive.risk.response.mail.text=Message",
    "cas.authn.adaptive.risk.response.mail.subject=Subject",

    "cas.authn.adaptive.risk.response.sms.text=Message",
    "cas.authn.adaptive.risk.response.sms.from=3487244312"
})
@Tag("Simple")
public class AuthenticationRiskSmsNotifierTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @Test
    public void verifyOperation() {
        try {
            authenticationRiskSmsNotifier.setRegisteredService(CoreAuthenticationTestUtils.getRegisteredService());
            val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("phone", List.of("3487244312")));
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
            authenticationRiskSmsNotifier.setAuthentication(authentication);
            authenticationRiskSmsNotifier.setAuthenticationRiskScore(new AuthenticationRiskScore(BigDecimal.ONE));
            authenticationRiskSmsNotifier.publish();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
