package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.config.ElectronicFenceWebflowConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

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
    ElectronicFenceWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("WebflowEvents")
@TestPropertySource(properties = {
    "cas.authn.adaptive.risk.ip.enabled=true",
    "cas.authn.adaptive.risk.response.sms.text=Message",
    "cas.authn.adaptive.risk.response.sms.from=3487244312"
})
public class RiskAwareAuthenticationWebflowEventResolverTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("riskAwareAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyNoResolution() {
        assertNotNull(riskAwareAuthenticationWebflowEventResolver);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertNull(riskAwareAuthenticationWebflowEventResolver.resolve(context));
    }

    @Test
    public void verifyResolution() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        assertEquals(TestMultifactorAuthenticationProvider.ID,
            riskAwareAuthenticationWebflowEventResolver.resolve(context).iterator().next().getId());
    }

}
