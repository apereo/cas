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
import org.springframework.test.annotation.DirtiesContext;
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
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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
        }
    }

    @Nested
    class DuplicateHandlers extends BaseTests {
        @Test
        void verifyDuplicateHandlers() {
            val h1 = new AcceptUsersAuthenticationHandler("Handler1");
            val h2 = new AcceptUsersAuthenticationHandler(h1.getName());
            assertEquals(h1, h2);
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h1));
            assertFalse(authenticationEventExecutionPlan.registerAuthenticationHandler(h2));
            h2.setState(AuthenticationHandlerStates.STANDBY);
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h2));
        }
    }

    @Nested
    class DefaultHandlers extends BaseTests {
        @Test
        void verifyDefaults() {
            val input = mock(AuthenticationEventExecutionPlan.class);
            when(input.getAuthenticationHandlers()).thenReturn(Set.of());
            when(input.getAuthenticationHandlersBy(any())).thenCallRealMethod();
            assertNotNull(input.getAuthenticationHandlersBy(handler -> false));
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
    class MatchingHandlers extends BaseTests {
        @Test
        void verifyMismatchedCount() {
            authenticationEventExecutionPlan.registerAuthenticationHandlersWithPrincipalResolver(List.of(new SimpleTestUsernamePasswordAuthenticationHandler()), List.of());
            assertTrue(authenticationEventExecutionPlan.getAuthenticationHandlers().isEmpty());
        }
    }

    @Nested
    class EmptyHandlers extends BaseTests {
        @Test
        void verifyNoHandlerResolves() {
            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                .newTransaction(CoreAuthenticationTestUtils.getWebApplicationService(), mock(Credential.class));
            assertThrows(AuthenticationException.class, () -> authenticationEventExecutionPlan.getAuthenticationHandlers(transaction));
        }
    }


    @Nested
    class CredentialTypes extends BaseTests {
        @Test
        void verifyByCredentialType() throws Throwable {
            val h1 = new SimpleTestUsernamePasswordAuthenticationHandler("Handler1");
            val h2 = new SimpleTestUsernamePasswordAuthenticationHandler("Handler2");
          
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h1));
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h2));

            val credential = new UsernamePasswordCredential();
            credential.setUsername(UUID.randomUUID().toString());
            credential.assignPassword(credential.getUsername());
            credential.setSource(h1.getName());

            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                .newTransaction(CoreAuthenticationTestUtils.getWebApplicationService(), credential);

            val handlers = authenticationEventExecutionPlan.getAuthenticationHandlers(transaction);
            assertEquals(1, handlers.size());
            assertEquals(h1, handlers.iterator().next());
        }
    }

    @Nested
    class CredentialTypeWithService extends BaseTests {
        @Test
        void verifyByCredentialTypeAndServiceByResolvers() throws Throwable {
            val h1 = new SimpleTestUsernamePasswordAuthenticationHandler("Handler1");
            val h2 = new SimpleTestUsernamePasswordAuthenticationHandler("Handler2");
            val h3 = mock(MultifactorAuthenticationHandler.class);
            when(h3.getName()).thenReturn("Handler3");
            
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h1));
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h2));
            assertTrue(authenticationEventExecutionPlan.registerAuthenticationHandler(h3));

            val credential = new UsernamePasswordCredential();
            credential.setUsername(UUID.randomUUID().toString());
            credential.assignPassword(credential.getUsername());
            credential.setSource(h1.getName());

            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            val authnPolicy = mock(RegisteredServiceAuthenticationPolicy.class);
            when(authnPolicy.getRequiredAuthenticationHandlers()).thenReturn(Set.of(h2.getName()));
            when(registeredService.getAuthenticationPolicy()).thenReturn(authnPolicy);

            val service = CoreAuthenticationTestUtils.getWebApplicationService();
            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(service, credential);
            when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

            val serviceSelectionPlan = new DefaultAuthenticationServiceSelectionPlan();
            serviceSelectionPlan.registerStrategy(new DefaultAuthenticationServiceSelectionStrategy());

            authenticationEventExecutionPlan.registerAuthenticationHandlerResolver(new ByCredentialSourceAuthenticationHandlerResolver());
            authenticationEventExecutionPlan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(servicesManager, serviceSelectionPlan));

            val handlers = authenticationEventExecutionPlan.getAuthenticationHandlers(transaction);
            assertEquals(2, handlers.size());
            assertTrue(handlers.containsAll(Set.of(h1, h3)));
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
