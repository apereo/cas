package org.apereo.cas.impl.notify;

import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.impl.calcs.BaseAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

/**
 * This is {@link AuthenticationRiskSmsNotifierTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(locations = "classpath:risk-electrofence.properties")
public class AuthenticationRiskSmsNotifierTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @Test
    public void verifyOperation() {
        try {
            authenticationRiskSmsNotifier.setRegisteredService(CoreAuthenticationTestUtils.getRegisteredService());
            val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("phone", "3487244312"));
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
            authenticationRiskSmsNotifier.setAuthentication(authentication);
            authenticationRiskSmsNotifier.setAuthenticationRiskScore(new AuthenticationRiskScore(BigDecimal.ONE));
            authenticationRiskSmsNotifier.publish();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
