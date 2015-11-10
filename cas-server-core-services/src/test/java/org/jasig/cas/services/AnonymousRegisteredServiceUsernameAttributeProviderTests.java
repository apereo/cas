package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class AnonymousRegisteredServiceUsernameAttributeProviderTests {

    @Test
    public void verifyPrincipalResolution() {
        final AnonymousRegisteredServiceUsernameAttributeProvider provider =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                new ShibbolethCompatiblePersistentIdGenerator("casrox"));
        
        final Service service = mock(Service.class);
        when(service.getId()).thenReturn("id");
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("uid");
        final String id = provider.resolveUsername(principal, service);
        assertNotNull(id);
    }

    @Test
    public void verifyEquality() {
        final AnonymousRegisteredServiceUsernameAttributeProvider provider =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                        new ShibbolethCompatiblePersistentIdGenerator("casrox"));

        final AnonymousRegisteredServiceUsernameAttributeProvider provider2 =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                        new ShibbolethCompatiblePersistentIdGenerator("casrox"));

        assertEquals(provider, provider2);
    }
}
