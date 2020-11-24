package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PersistentIdGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class PersistentIdGeneratorTests {
    @Test
    public void verifyOperation() {
        val policy = mock(PersistentIdGenerator.class);
        when(policy.generate(any(Principal.class))).thenCallRealMethod();
        when(policy.generate(any(Principal.class), anyString())).thenReturn("1");
        assertEquals("1", policy.generate(mock(Principal.class)));
    }
}
