package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.core.ticket.RememberMeAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
public class DefaultAuthenticationEventExecutionPlanTests {
    @Test
    public void verifyDuplicateHandlers() throws Exception {
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
    public void verifyOperation() {
        val context = PrincipalResolutionContext.builder()
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationPreProcessor(transaction -> false);
        plan.registerAuthenticationMetadataPopulators(
            Set.of(new RememberMeAuthenticationMetaDataPopulator(new RememberMeAuthenticationProperties())));
        plan.registerAuthenticationHandlerWithPrincipalResolvers(
            Set.of(new SimpleTestUsernamePasswordAuthenticationHandler()), new PersonDirectoryPrincipalResolver(context));
        plan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        plan.registerAuthenticationPolicyResolver(transaction -> Set.of(new AllCredentialsValidatedAuthenticationPolicy()));
        assertFalse(plan.getAuthenticationPolicies(
            new DefaultAuthenticationTransactionFactory().newTransaction(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())).isEmpty());
    }

    @Test
    public void verifyMismatchedCount() {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlerWithPrincipalResolvers(List.of(new SimpleTestUsernamePasswordAuthenticationHandler()), List.of());
        assertTrue(plan.getAuthenticationHandlers().isEmpty());
    }


    @Test
    public void verifyNoHandlerResolves() {
        val transaction = new DefaultAuthenticationTransaction(CoreAuthenticationTestUtils.getService(),
            List.of(mock(Credential.class)));
        val plan = new DefaultAuthenticationEventExecutionPlan();
        assertThrows(AuthenticationException.class, () -> plan.getAuthenticationHandlers(transaction));
    }


    @Test
    public void verifyDefaults() {
        val input = mock(AuthenticationEventExecutionPlan.class);
        when(input.getAuthenticationHandlers()).thenReturn(Set.of());
        when(input.getAuthenticationHandlersBy(any())).thenCallRealMethod();
        assertNotNull(input.getAuthenticationHandlersBy(authenticationHandler -> false));
    }

}
