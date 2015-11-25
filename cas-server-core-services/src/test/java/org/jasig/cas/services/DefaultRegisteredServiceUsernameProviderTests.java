package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Principal;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultRegisteredServiceUsernameProviderTests {
    
    @Test
    public void verifyRegServiceUsername() {
        final DefaultRegisteredServiceUsernameProvider provider = 
                new DefaultRegisteredServiceUsernameProvider();
        
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        final String id = provider.resolveUsername(principal, TestUtils.getService());
        assertEquals(id, principal.getId());
    }

    @Test
    public void verifyEquality() {
        final DefaultRegisteredServiceUsernameProvider provider =
                new DefaultRegisteredServiceUsernameProvider();

        final DefaultRegisteredServiceUsernameProvider provider2 =
                new DefaultRegisteredServiceUsernameProvider();

        assertEquals(provider, provider2);
    }
}
