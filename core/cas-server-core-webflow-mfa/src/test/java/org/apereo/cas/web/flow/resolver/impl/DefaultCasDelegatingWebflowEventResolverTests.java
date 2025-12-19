package org.apereo.cas.web.flow.resolver.impl;

import module java.base;
import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasDelegatingWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
class DefaultCasDelegatingWebflowEventResolverTests {
    @Nested
    class DefaultTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Test
        void verifyOperationNoCredential() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
        }

        @Test
        void verifyAuthFails() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val id = "https://app.%s.org#helloworld".formatted(UUID.randomUUID().toString());
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            servicesManager.save(registeredService);
            WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword(id));
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
            assertTrue(event.getAttributes().contains(Credential.class.getName()));
            assertTrue(event.getAttributes().contains(WebApplicationService.class.getName()));
            val service = (WebApplicationService) event.getAttributes().get(WebApplicationService.class.getName());
            assertNotNull(service);
            assertEquals(service.getId(), id);
            assertEquals(service.getOriginalUrl(), id);
        }

        @Test
        void verifyServiceDisallowed() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val id = UUID.randomUUID().toString();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
            servicesManager.save(registeredService);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }

        @Test
        void verifyNoAuthn() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val id = UUID.randomUUID().toString();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            servicesManager.save(registeredService);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
        }

        @Test
        void verifyCanCatchUriParsingException() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val requestContext = new MockHttpServletRequest();
            requestContext.setParameter("service", "https://init.cas.org/?q=Illegal Character");

            context.setExternalContext(new ServletExternalContext(null, requestContext, new MockHttpServletResponse()));

            val id = UUID.randomUUID().toString();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
            servicesManager.save(registeredService);

            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }
    }

    @Nested
    @Import(MfaTests.MfaTestConfiguration.class)
    @TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy")
    class MfaTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Test
        void verifyOperationResolvesToEvent() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
            val transition = new Transition(new DefaultTransitionCriteria(
                new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
            context.getRootFlow().getGlobalTransitionSet().add(transition);
            
            val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
            WebUtils.putAuthentication(authn, context);
            context.setParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(),
                TestMultifactorAuthenticationProvider.ID);
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(TestMultifactorAuthenticationProvider.ID, event.getId());
        }

        @TestConfiguration(value = "MfaTestConfiguration", proxyBeanMethods = false)
        static class MfaTestConfiguration {
            @Bean
            public MultifactorAuthenticationProvider dummyProvider() {
                return new TestMultifactorAuthenticationProvider();
            }
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class TenantTests extends BaseCasWebflowMultifactorAuthenticationTests {

        @Test
        void verifyTenantCaptured() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setContextPath("/tenants/shire/login");
            val id = "https://app.%s.org#helloworld".formatted(UUID.randomUUID().toString());
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id, Map.of());
            servicesManager.save(registeredService);

            val credential = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword(id);
            WebUtils.putCredential(context, credential);
            val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            assertEquals("shire", credential.getCredentialMetadata().getTenant());
        }
    }
}
