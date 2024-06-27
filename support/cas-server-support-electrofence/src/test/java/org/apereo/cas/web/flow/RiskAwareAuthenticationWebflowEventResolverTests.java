package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasElectronicFenceAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ImportAutoConfiguration({
    CasCoreEventsAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasElectronicFenceAutoConfiguration.class
})
@Tag("WebflowEvents")
@TestPropertySource(properties = {
    "cas.authn.adaptive.risk.ip.enabled=true",
    "cas.authn.adaptive.risk.response.sms.text=Message",
    "cas.authn.adaptive.risk.response.sms.from=3487244312"
})
class RiskAwareAuthenticationWebflowEventResolverTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("riskAwareAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver;
    
    @Test
    void verifyNoResolution() throws Throwable {
        assertNotNull(riskAwareAuthenticationWebflowEventResolver);
        val context = MockRequestContext.create(applicationContext);
        assertNull(riskAwareAuthenticationWebflowEventResolver.resolve(context));
    }

    @Test
    void verifyResolution() throws Throwable {
        val context = MockRequestContext.create(applicationContext).setClientInfo();
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        assertEquals(TestMultifactorAuthenticationProvider.ID,
            riskAwareAuthenticationWebflowEventResolver.resolve(context).iterator().next().getId());
    }

}
