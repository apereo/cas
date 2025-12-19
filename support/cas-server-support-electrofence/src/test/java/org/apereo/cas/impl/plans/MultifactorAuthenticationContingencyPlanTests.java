package org.apereo.cas.impl.plans;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationContingencyPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class MultifactorAuthenticationContingencyPlanTests {

    @Test
    void verifyNoProvider() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val props = new CasConfigurationProperties();

        val plan = new MultifactorAuthenticationContingencyPlan(props, appCtx);
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("mail", List.of("cas@example.org")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        assertThrows(AuthenticationException.class, () -> plan.execute(authentication, registeredService,
            AuthenticationRiskScore.highestRiskScore(), new MockHttpServletRequest()));

    }

    @Test
    void verifyManyProviders() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val props = new CasConfigurationProperties();

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx, new TestMultifactorAuthenticationProvider());
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx, new TestMultifactorAuthenticationProvider("mfa-two"));

        val plan = new MultifactorAuthenticationContingencyPlan(props, appCtx);
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("mail", List.of("cas@example.org")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        assertThrows(AuthenticationException.class, () -> plan.execute(authentication, registeredService,
            AuthenticationRiskScore.highestRiskScore(), new MockHttpServletRequest()));

    }

}
