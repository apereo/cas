package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class ServiceTests {
    @Test
    void verifyOperation() {
        val policy = mock(Service.class);
        doCallRealMethod().when(policy).setPrincipal(anyString());
        assertDoesNotThrow(() -> policy.setPrincipal("casuser"));
    }
}
