package org.jasig.cas.helper;

import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultCredentialsToPrincipalResolverTest extends TestCase {

    private CredentialsToPrincipalResolver resolver = new DefaultCredentialsToPrincipalResolver();

    public void testValidCredentials() {
        UsernamePasswordCredentials request = new UsernamePasswordCredentials();
        request.setUserName("test");
        Principal p = this.resolver.resolvePrincipal(request);

        assertEquals(p.getId(), request.getUserName());
    }

    public void testInvalidCredentials() {
        UsernamePasswordCredentials request = new UsernamePasswordCredentials();
        request.setUserName(null);
        try {
            this.resolver.resolvePrincipal(request);
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
