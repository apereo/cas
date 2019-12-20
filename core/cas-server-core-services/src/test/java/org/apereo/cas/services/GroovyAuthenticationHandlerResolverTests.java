package org.apereo.cas.services;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.GroovyAuthenticationHandlerResolver;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
public class GroovyAuthenticationHandlerResolverTests {
    @Test
    public void verifyAction() {
        val resolver = new GroovyAuthenticationHandlerResolver(
            new ClassPathResource("GroovyAuthenticationHandlerResolver.groovy"),
            mock(ServicesManager.class));

        val transaction = mock(AuthenticationTransaction.class);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        when(transaction.getPrimaryCredential()).thenReturn(Optional.of(creds));

        assertTrue(resolver.supports(Set.of(new AcceptUsersAuthenticationHandler("casuser")), transaction));

        val results = resolver.resolve(Set.of(new AcceptUsersAuthenticationHandler("casuser")), transaction);
        assertFalse(results.isEmpty());
    }
}
