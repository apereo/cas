package org.apereo.cas.authentication;

import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link DisablePasswordlessTokenAuthenticationTests}.
 */
@TestPropertySource(properties = "cas.authn.passwordless.core.enabled=false")
@Tag("AuthenticationHandler")
public class DisablePasswordlessTokenAuthenticationTests extends BasePasswordlessUserAccountStoreTests {

    @Autowired
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    @Test
    public void verifyDisable() {
        Set<AuthenticationHandler> authenticationHandlers =
                authenticationEventExecutionPlan.getAuthenticationHandlersBy(
                        h -> h instanceof PasswordlessTokenAuthenticationHandler);
        assertTrue(authenticationHandlers.isEmpty());
    }
}
