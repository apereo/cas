package org.apereo.cas.authentication.handler.support;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AuthenticationHandler")
class DefaultAuthenticationHandlerResolverTests {
    @Test
    void verifyOperation() throws Throwable {
        val h1 = new SimpleTestUsernamePasswordAuthenticationHandler("h1");
        val h2 = new SimpleTestUsernamePasswordAuthenticationHandler("h2");
        h2.setState(AuthenticationHandlerStates.STANDBY);
        val resolver = new DefaultAuthenticationHandlerResolver();
        assertTrue(resolver.supports(Set.of(h1, h2), mock(AuthenticationTransaction.class)));
        val result = resolver.resolve(Set.of(h1, h2), mock(AuthenticationTransaction.class));
        assertTrue(result.contains(h1));
        assertFalse(result.contains(h2));
        assertEquals(Ordered.LOWEST_PRECEDENCE, resolver.getOrder());
    }

    @Test
    void verifyNoOp() throws Throwable {
        assertFalse(AuthenticationHandlerResolver.noOp().supports(Set.of(), mock(AuthenticationTransaction.class)));
        assertTrue(AuthenticationHandlerResolver.noOp().resolve(Set.of(), mock(AuthenticationTransaction.class)).isEmpty());
    }
}
