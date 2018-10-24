package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ChainingPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class ChainingPrincipalResolverTests {

    private final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    @Test
    public void examineSupports() {
        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("a");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(eq(credential))).thenReturn(false);

        val resolver = new ChainingPrincipalResolver();
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        assertTrue(resolver.supports(credential));
    }

    @Test
    public void examineResolve() {
        val principalOut = principalFactory.createPrincipal("output");
        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve(eq(credential), any(Optional.class), any(Optional.class))).thenReturn(principalOut);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(true);
        when(resolver2.resolve(any(Credential.class), any(Optional.class), any(Optional.class)))
            .thenReturn(principalFactory.createPrincipal("output", Collections.singletonMap("mail", "final@example.com")));

        val resolver = new ChainingPrincipalResolver();
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        val principal = resolver.resolve(credential,
            Optional.of(principalOut),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals("output", principal.getId());
        assertEquals("final@example.com", CollectionUtils.firstElement(principal.getAttributes().get("mail")).get());
    }

}
