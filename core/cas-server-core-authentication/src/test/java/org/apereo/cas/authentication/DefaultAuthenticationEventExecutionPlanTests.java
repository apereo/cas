package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

import lombok.val;
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
    public void verifyOperation() {
        val plan = new DefaultAuthenticationEventExecutionPlan(CoreAuthenticationTestUtils.getAuthenticationSystemSupport());
        plan.registerAuthenticationPreProcessor(transaction -> false);
        plan.registerAuthenticationMetadataPopulators(
            Set.of(new RememberMeAuthenticationMetaDataPopulator()));
        plan.registerAuthenticationHandlerWithPrincipalResolvers(
            Set.of(new SimpleTestUsernamePasswordAuthenticationHandler()), new PersonDirectoryPrincipalResolver());
        plan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        plan.registerAuthenticationPolicyResolver(transaction -> Set.of(new AllCredentialsValidatedAuthenticationPolicy()));
        assertFalse(plan.getAuthenticationPolicies(
            DefaultAuthenticationTransaction.of(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())).isEmpty());
    }

    @Test
    public void verifyMismatchedCount() {
        val plan = new DefaultAuthenticationEventExecutionPlan(CoreAuthenticationTestUtils.getAuthenticationSystemSupport());
        plan.registerAuthenticationHandlerWithPrincipalResolvers(List.of(new SimpleTestUsernamePasswordAuthenticationHandler()), List.of());
        assertTrue(plan.getAuthenticationHandlers().isEmpty());
    }


    @Test
    public void verifyNoHandlerResolves() {
        val transaction = new DefaultAuthenticationTransaction(CoreAuthenticationTestUtils.getService(),
            List.of(mock(Credential.class)));
        val plan = new DefaultAuthenticationEventExecutionPlan(CoreAuthenticationTestUtils.getAuthenticationSystemSupport());
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
