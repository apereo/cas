package org.apereo.cas.oidc.web.flow;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAuthenticationContextWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.authn.oidc.discovery.acr-values-supported=mfa-dummy")
@Import(OidcAuthenticationContextWebflowEventResolverTests.OidcAuthenticationContextTestConfiguration.class)
class OidcAuthenticationContextWebflowEventResolverTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAuthenticationContextWebflowEventResolver")
    protected CasWebflowEventResolver resolver;

    @Test
    void verifyOperationNeedsMfa() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("385.86.151.11");
        context.setLocalAddr("295.88.151.11");
        context.withUserAgent();
        context.setParameter(OAuth20Constants.ACR_VALUES, TestMultifactorAuthenticationProvider.ID);
        context.setClientInfo();

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        
        val event = resolver.resolve(context);
        assertEquals(1, event.size());
        assertEquals(TestMultifactorAuthenticationProvider.ID, event.iterator().next().getId());
    }

    @TestConfiguration(value = "OidcAuthenticationContextTestConfiguration", proxyBeanMethods = false)
    static class OidcAuthenticationContextTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
