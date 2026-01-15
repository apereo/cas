package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceDelegatedAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class RegisteredServiceDelegatedAuthenticationPolicyTests {

    @Test
    void verifyOperation() {
        val policy = mock(RegisteredServiceDelegatedAuthenticationPolicy.class);
        when(policy.isProviderAllowed(anyString(), any())).thenCallRealMethod();
        assertTrue(policy.isProviderAllowed("hello", mock(RegisteredService.class)));
    }

}
