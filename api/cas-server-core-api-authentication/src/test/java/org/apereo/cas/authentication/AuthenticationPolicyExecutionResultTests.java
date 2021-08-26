package org.apereo.cas.authentication;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthenticationPolicyExecutionResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AuthenticationPolicy")
public class AuthenticationPolicyExecutionResultTests {

    @Test
    public void verifyOperation() {
        assertTrue(AuthenticationPolicyExecutionResult.success(true).isSuccess());
        assertTrue(AuthenticationPolicyExecutionResult.success().isSuccess());
        assertFalse(AuthenticationPolicyExecutionResult.failure().isSuccess());
    }
}
