package org.apereo.cas.impl.notify;

import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.impl.calcs.BaseAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

/**
 * This is {@link AuthenticationRiskEmailNotifierTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfPortOpen(port = 25000)
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.authn.adaptive.risk.response.mail.from=cas@example.org",
    "cas.authn.adaptive.risk.response.mail.text=Message",
    "cas.authn.adaptive.risk.response.mail.subject=Subject",

    "cas.authn.adaptive.risk.response.sms.text=Message",
    "cas.authn.adaptive.risk.response.sms.from=3487244312"
})
@Import(MailSenderAutoConfiguration.class)
@Tag("Mail")
public class AuthenticationRiskEmailNotifierTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @Test
    public void verifyOperation() {
        try {
            authenticationRiskEmailNotifier.setRegisteredService(CoreAuthenticationTestUtils.getRegisteredService());
            val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("mail", List.of("cas@example.org")));
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
            authenticationRiskEmailNotifier.setAuthentication(authentication);
            authenticationRiskEmailNotifier.setAuthenticationRiskScore(new AuthenticationRiskScore(BigDecimal.ONE));
            authenticationRiskEmailNotifier.publish();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
