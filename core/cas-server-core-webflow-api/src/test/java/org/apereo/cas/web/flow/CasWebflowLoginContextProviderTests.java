package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
public class CasWebflowLoginContextProviderTests {
    @Test
    public void verifyOperation() {
        val provider = mock(CasWebflowLoginContextProvider.class);
        when(provider.getOrder()).thenCallRealMethod();
        when(provider.getName()).thenCallRealMethod();
        when(provider.getCandidateUsername(any())).thenReturn(Optional.of("cas"));
        assertEquals(Ordered.LOWEST_PRECEDENCE, provider.getOrder());
        assertTrue(provider.getCandidateUsername(new MockRequestContext()).isPresent());
        assertNotNull(provider.getName());
    }
}
