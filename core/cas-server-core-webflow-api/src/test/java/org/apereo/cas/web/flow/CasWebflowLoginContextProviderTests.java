package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
class CasWebflowLoginContextProviderTests {
    @Test
    void verifyOperation() throws Throwable {
        val provider = mock(CasWebflowLoginContextProvider.class);
        when(provider.getOrder()).thenCallRealMethod();
        when(provider.getName()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, provider.getOrder());
        assertNotNull(provider.getName());
    }
}
