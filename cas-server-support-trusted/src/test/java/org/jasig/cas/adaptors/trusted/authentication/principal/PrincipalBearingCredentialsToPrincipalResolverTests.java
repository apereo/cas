package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class PrincipalBearingCredentialsToPrincipalResolverTests {
    private PrincipalBearingPrincipalResolver resolver;

    @Before
    public void setUp() throws Exception {
        this.resolver = new PrincipalBearingPrincipalResolver();
    }

    @Test
    public void verifySupports() {
        assertTrue(this.resolver.supports(new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("test"))));
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
        assertFalse(this.resolver.supports(null));
    }

    @Test
    public void verifyReturnedPrincipal() {
        assertEquals("test", this.resolver.resolve(
                new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("test"))).getId());
    }

}
