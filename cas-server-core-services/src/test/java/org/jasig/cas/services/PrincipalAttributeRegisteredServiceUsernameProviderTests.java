package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Principal;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class PrincipalAttributeRegisteredServiceUsernameProviderTests {
    @Test
    public void verifyUsernameByPrincipalAttribute() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("userid", "u1");
        attrs.put("cn", "TheName");
        
        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);
        
        final String id = provider.resolveUsername(p, TestUtils.getService("proxyService"));
        assertEquals(id, "TheName");
        
    }
    
    @Test
    public void verifyUsernameByPrincipalAttributeNotFound() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("userid", "u1");
                
        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);
        
        final String id = provider.resolveUsername(p, TestUtils.getService("proxyService"));
        assertEquals(id, p.getId());
        
    }

    @Test
    public void verifyEquality() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        final PrincipalAttributeRegisteredServiceUsernameProvider provider2 =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        assertEquals(provider, provider2);
    }

}
