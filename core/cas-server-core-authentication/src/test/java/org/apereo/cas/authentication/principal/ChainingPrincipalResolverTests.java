package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ChainingPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class ChainingPrincipalResolverTests {

    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @Test
    public void examineSupports() {
        final Credential credential = mock(Credential.class);
        when(credential.getId()).thenReturn("a");

        final PrincipalResolver resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);

        final PrincipalResolver resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(eq(credential))).thenReturn(false);

        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        assertTrue(resolver.supports(credential));
    }

    @Test
    public void examineResolve() {
        final Principal principalOut = principalFactory.createPrincipal("output");
        final Credential credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        final PrincipalResolver resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve(eq(credential), any(Principal.class),
                any(AuthenticationHandler.class)))
                .thenReturn(principalOut);

        final PrincipalResolver resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(true);
        when(resolver2.resolve(any(Credential.class), any(Principal.class),
                any(AuthenticationHandler.class)))
                .thenReturn(principalFactory.createPrincipal("output", Collections.singletonMap("mail", "final@example.com")));

        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        final Principal principal = resolver.resolve(credential,
                principalOut, 
                new SimpleTestUsernamePasswordAuthenticationHandler());
        assertEquals("output", principal.getId());
        assertEquals("final@example.com", principal.getAttributes().get("mail"));
    }

}
