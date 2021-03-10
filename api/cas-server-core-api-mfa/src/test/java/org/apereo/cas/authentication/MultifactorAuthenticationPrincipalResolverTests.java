package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFA")
public class MultifactorAuthenticationPrincipalResolverTests {
    @Test
    public void verifyOperation() {
        val resolver = MultifactorAuthenticationPrincipalResolver.identical();
        assertEquals(Ordered.LOWEST_PRECEDENCE, resolver.getOrder());

        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(principal.getAttributes()).thenReturn(Map.of());
        assertTrue(resolver.supports(principal));
        assertEquals(principal, resolver.resolve(principal));
    }
}
