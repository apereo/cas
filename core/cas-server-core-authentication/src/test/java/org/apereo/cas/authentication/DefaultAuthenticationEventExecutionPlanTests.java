package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.core.ticket.RememberMeAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthenticationEventExecutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
class DefaultAuthenticationEventExecutionPlanTests {
    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @BeforeEach
    public void before() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void verifyDuplicateHandlers() throws Throwable {
        val h1 = new AcceptUsersAuthenticationHandler("Handler1");
        val h2 = new AcceptUsersAuthenticationHandler(h1.getName());
        assertEquals(h1, h2);

        val plan = new DefaultAuthenticationEventExecutionPlan();
        assertTrue(plan.registerAuthenticationHandler(h1));
        assertFalse(plan.registerAuthenticationHandler(h2));
        h2.setState(AuthenticationHandlerStates.STANDBY);
        assertTrue(plan.registerAuthenticationHandler(h2));
    }

    @Test
    void verifyOperation() throws Throwable {
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

        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationPreProcessor(transaction -> false);
        plan.registerAuthenticationMetadataPopulators(
            Set.of(new RememberMeAuthenticationMetaDataPopulator(new RememberMeAuthenticationProperties())));
        plan.registerAuthenticationHandlersWithPrincipalResolver(
            Set.of(new SimpleTestUsernamePasswordAuthenticationHandler()), new PersonDirectoryPrincipalResolver(context));
        plan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        plan.registerAuthenticationPolicyResolver(transaction -> Set.of(new AllCredentialsValidatedAuthenticationPolicy()));
        assertFalse(plan.getAuthenticationPolicies(
            CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())).isEmpty());
    }

    @Test
    void verifyMismatchedCount() throws Throwable {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlersWithPrincipalResolver(List.of(new SimpleTestUsernamePasswordAuthenticationHandler()), List.of());
        assertTrue(plan.getAuthenticationHandlers().isEmpty());
    }


    @Test
    void verifyNoHandlerResolves() throws Throwable {
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getWebApplicationService(), mock(Credential.class));
        val plan = new DefaultAuthenticationEventExecutionPlan();
        assertThrows(AuthenticationException.class, () -> plan.getAuthenticationHandlers(transaction));
    }


    @Test
    void verifyDefaults() throws Throwable {
        val input = mock(AuthenticationEventExecutionPlan.class);
        when(input.getAuthenticationHandlers()).thenReturn(Set.of());
        when(input.getAuthenticationHandlersBy(any())).thenCallRealMethod();
        assertNotNull(input.getAuthenticationHandlersBy(handler -> false));
    }

}
