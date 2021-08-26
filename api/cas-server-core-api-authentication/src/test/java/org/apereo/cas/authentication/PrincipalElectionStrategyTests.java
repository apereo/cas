package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrincipalElectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class PrincipalElectionStrategyTests {
    @Test
    public void verifyOperation() {
        val policy = mock(PrincipalElectionStrategy.class);
        when(policy.getOrder()).thenCallRealMethod();
        assertEquals(Integer.MAX_VALUE, policy.getOrder());
    }
}
