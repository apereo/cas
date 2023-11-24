package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreEventsConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.config.ElectronicFenceWebflowConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import({
    CasCoreEventsConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    ElectronicFenceConfiguration.class,
    ElectronicFenceWebflowConfiguration.class
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
        val context = MockRequestContext.create(applicationContext);

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        assertEquals(TestMultifactorAuthenticationProvider.ID,
            riskAwareAuthenticationWebflowEventResolver.resolve(context).iterator().next().getId());
    }

}
