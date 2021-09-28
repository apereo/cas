package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RankedMultifactorAuthenticationProviderWebflowEventResolverWithCompositeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowEvents")
@Import(RankedMultifactorAuthenticationProviderWebflowEventResolverWithCompositeTests.MultifactorTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.mfa.core.provider-selection-enabled=true",
    "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy1,mfa-dummy2"
})
public class RankedMultifactorAuthenticationProviderWebflowEventResolverWithCompositeTests
    extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
    private CasDelegatingWebflowEventResolver resolver;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService cas;

    @BeforeEach
    public void setup() {
        super.setup();
        this.servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        cas.addTicket(tgt);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        val multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
        registeredService.setMultifactorPolicy(multifactorPolicy);
        servicesManager.save(registeredService);
        WebUtils.putRegisteredService(context, registeredService);

        val targetResolver = new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_MFA_COMPOSITE);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(CasWebflowConstants.STATE_ID_MFA_COMPOSITE)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        
        assertEquals(CasWebflowConstants.STATE_ID_MFA_COMPOSITE, resolver.resolveSingle(context).getId());
    }

    @TestConfiguration("MultifactorTestConfiguration")
    public static class MultifactorTestConfiguration {

        @Bean
        public MultifactorAuthenticationProvider dummyProvider1() {
            return new TestMultifactorAuthenticationProvider("mfa-dummy1");
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProvider2() {
            return new TestMultifactorAuthenticationProvider("mfa-dummy2");
        }
    }
}
