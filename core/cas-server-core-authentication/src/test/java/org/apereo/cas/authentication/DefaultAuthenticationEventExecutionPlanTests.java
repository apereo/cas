package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.ByCredentialSourceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.core.ticket.RememberMeAuthenticationProperties;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthenticationEventExecutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class DefaultAuthenticationEventExecutionPlanTests {
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        DefaultAuthenticationEventExecutionPlanTests.AuthenticationPlanTestConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class
    }, properties = "cas.sso.proxy-authn-enabled=false")
    @ExtendWith(CasTestExtension.class)
    abstract static class BaseTests {
        protected AttributeRepositoryResolver attributeRepositoryResolver;

        protected AttributeDefinitionStore attributeDefinitionStore;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;

        @Autowired
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        protected AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        protected ServicesManager servicesManager;

        @BeforeEach
        void before() {
            this.attributeRepositoryResolver = mock(AttributeRepositoryResolver.class);
            this.attributeDefinitionStore = mock(AttributeDefinitionStore.class);
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("223.456.789.000");
            request.setLocalAddr("223.456.789.100");
            request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        }
    }

    @Nested
    @Import(DuplicateHandlers.AuthenticationTestConfiguration.class)
    class DuplicateHandlers extends BaseTests {
        @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
        static class AuthenticationTestConfiguration {
            @Bean
            public AuthenticationEventExecutionPlanConfigurer authenticationPlanConfigurer() {
                return plan -> {
                    val h1 = new AcceptUsersAuthenticationHandler("Handler1");
                    val h2 = new AcceptUsersAuthenticationHandler(h1.getName());
                    assertEquals(h1, h2);

                    assertTrue(plan.registerAuthenticationHandler(h1));
                    assertFalse(plan.registerAuthenticationHandler(h2));
                    h2.setState(AuthenticationHandlerStates.STANDBY);
                    assertTrue(plan.registerAuthenticationHandler(h2));
                };
            }
        }
        
        @Test
        void verifyDuplicateHandlers() {
            assertEquals(2, authenticationEventExecutionPlan.resolveAuthenticationHandlers().size());
        }
    }

    @Nested
    class DefaultHandlers extends BaseTests {
        @Test
        void verifyDefaults() {
            val input = mock(AuthenticationEventExecutionPlan.class);
            when(input.resolveAuthenticationHandlers()).thenReturn(Set.of());
            when(input.resolveAuthenticationHandlersBy(any())).thenCallRealMethod();
            assertNotNull(input.resolveAuthenticationHandlersBy(handler -> false));
        }
        
        @Test
        void verifyOperation() {
            val context = PrincipalResolutionContext.builder()
                .servicesManager(servicesManager)
                .attributeRepositoryResolver(attributeRepositoryResolver)
                .attributeDefinitionStore(attributeDefinitionStore)
                .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
                .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
                .returnNullIfNoAttributes(false)
                .principalNameTransformer(formUserId -> formUserId)
                .useCurrentPrincipalId(false)
                .resolveAttributes(true)
                .applicationContext(applicationContext)
                .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
                .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
                .build();

            authenticationEventExecutionPlan.registerAuthenticationPreProcessor(transaction -> false);
            authenticationEventExecutionPlan.registerAuthenticationMetadataPopulators(
                Set.of(new RememberMeAuthenticationMetaDataPopulator(new RememberMeAuthenticationProperties())));
            authenticationEventExecutionPlan.registerAuthenticationHandlersWithPrincipalResolver(
                Set.of(new SimpleTestUsernamePasswordAuthenticationHandler()), new PersonDirectoryPrincipalResolver(context));
            authenticationEventExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
            authenticationEventExecutionPlan.registerAuthenticationPolicyResolver(transaction -> Set.of(new AllCredentialsValidatedAuthenticationPolicy()));
            assertFalse(authenticationEventExecutionPlan.getAuthenticationPolicies(
                CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(
                    CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())).isEmpty());
        }
    }

    @Nested
    @Import(MatchingHandlers.AuthenticationTestConfiguration.class)
    class MatchingHandlers extends BaseTests {
        @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
        static class AuthenticationTestConfiguration {
            @Bean
            public AuthenticationEventExecutionPlanConfigurer authenticationPlanConfigurer() {
                return plan -> {
                    plan.registerAuthenticationHandlersWithPrincipalResolver(List.of(new SimpleTestUsernamePasswordAuthenticationHandler()), List.of());
                };
            }
        }
        
        @Test
        void verifyMismatchedCount() {
            assertTrue(authenticationEventExecutionPlan.resolveAuthenticationHandlers().isEmpty());
        }
    }

    @Nested
    @Import(EmptyHandlers.AuthenticationTestConfiguration.class)
    class EmptyHandlers extends BaseTests {
        @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
        static class AuthenticationTestConfiguration {
        }
        
        @Test
        void verifyNoHandlerResolves() {
            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                .newTransaction(CoreAuthenticationTestUtils.getWebApplicationService(), mock(Credential.class));
            assertThrows(AuthenticationException.class, () -> authenticationEventExecutionPlan.resolveAuthenticationHandlers(transaction));
        }
    }

    @Nested
    @Import(CredentialTypes.AuthenticationTestConfiguration.class)
    class CredentialTypes extends BaseTests {
        @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
        static class AuthenticationTestConfiguration {
            @Bean
            public AuthenticationEventExecutionPlanConfigurer authenticationPlanConfigurer() {
                return plan -> {
                    plan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("Handler1"));
                    plan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("Handler2"));
                };
            }
        }

        @Test
        void verifyByCredentialType() throws Throwable {
            val credential = new UsernamePasswordCredential();
            credential.setUsername(UUID.randomUUID().toString());
            credential.assignPassword(credential.getUsername());
            credential.setSource("Handler1");

            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                .newTransaction(CoreAuthenticationTestUtils.getWebApplicationService(), credential);

            val handlers = authenticationEventExecutionPlan.resolveAuthenticationHandlers(transaction);
            assertEquals(1, handlers.size());
            assertEquals("Handler1", handlers.iterator().next().getName());
        }
    }

    @Nested
    @Import(CredentialTypeWithService.AuthenticationTestConfiguration.class)
    class CredentialTypeWithService extends BaseTests {
        @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
        static class AuthenticationTestConfiguration {
            @Bean
            public AuthenticationEventExecutionPlanConfigurer authenticationPlanConfigurer(
                @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
                return plan -> {
                    plan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("Handler1"));
                    plan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("Handler2"));
                    val h3 = mock(MultifactorAuthenticationHandler.class);
                    when(h3.getName()).thenReturn("Handler3");
                    plan.registerAuthenticationHandler(h3);

                    plan.registerAuthenticationHandlerResolver(new ByCredentialSourceAuthenticationHandlerResolver());

                    val serviceSelectionPlan = new DefaultAuthenticationServiceSelectionPlan();
                    serviceSelectionPlan.registerStrategy(new DefaultAuthenticationServiceSelectionStrategy());

                    plan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(servicesManager, serviceSelectionPlan));
                };
            }
        }
        
        @Test
        void verifyByCredentialTypeAndServiceByResolvers() throws Throwable {
            val credential = new UsernamePasswordCredential();
            credential.setUsername(UUID.randomUUID().toString());
            credential.assignPassword(credential.getUsername());
            credential.setSource("Handler1");

            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            val authnPolicy = mock(RegisteredServiceAuthenticationPolicy.class);
            when(authnPolicy.getRequiredAuthenticationHandlers()).thenReturn(Set.of("Handler2"));
            when(registeredService.getAuthenticationPolicy()).thenReturn(authnPolicy);

            val service = CoreAuthenticationTestUtils.getWebApplicationService();
            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(service, credential);
            when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
            val handlers = authenticationEventExecutionPlan.resolveAuthenticationHandlers(transaction);
            assertEquals(2, handlers.size());
            assertTrue(handlers.stream().anyMatch(h -> "Handler1".equalsIgnoreCase(h.getName())));
            assertTrue(handlers.stream().anyMatch(h -> "Handler3".equalsIgnoreCase(h.getName())));
        }
    }

    @TestConfiguration(value = "AuthenticationPlanTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AuthenticationPlanTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mock(ServicesManager.class);
        }
    }

}
