package org.jasig.cas.helper;

import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultCredentialsToPrincipalResolverTest extends TestCase {

    private CredentialsToPrincipalResolver resolver = new DefaultCredentialsToPrincipalResolver();

    public void testValidCredentials() {
        UsernamePasswordAuthenticationRequest request = new UsernamePasswordAuthenticationRequest();
        request.setUserName("test");
        Principal p = resolver.resolvePrincipal(request);

        assertEquals(p.getId(), request.getUserName());
    }

    public void testInvalidCredentials() {
        UsernamePasswordAuthenticationRequest request = new UsernamePasswordAuthenticationRequest();
        request.setUserName(null);
        try {
            Principal p = resolver.resolvePrincipal(request);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        catch (Exception e) {
            fail("IllegalArgumentException expected.");
        }

        fail("IllegalArgumentException expected.");
    }
}
