package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
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
@Tag("Webflow")
class CasWebflowLoginContextProviderTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();

        val provider = mock(CasWebflowLoginContextProvider.class);
        when(provider.getOrder()).thenCallRealMethod();
        when(provider.getName()).thenCallRealMethod();
        when(provider.getCandidateUsername(any())).thenCallRealMethod();
        when(provider.isLoginFormViewable(any())).thenCallRealMethod();
        when(provider.isLoginFormUsernameInputVisible(any())).thenCallRealMethod();
        when(provider.isLoginFormUsernameInputDisabled(any())).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, provider.getOrder());
        assertNotNull(provider.getName());
        assertFalse(provider.isLoginFormViewable(context));
        assertFalse(provider.isLoginFormUsernameInputVisible(context));
        assertFalse(provider.isLoginFormUsernameInputDisabled(context));
        assertFalse(provider.getCandidateUsername(context).isPresent());
    }
}
