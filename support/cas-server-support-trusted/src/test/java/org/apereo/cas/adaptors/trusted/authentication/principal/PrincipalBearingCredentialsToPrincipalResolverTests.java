package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
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
    public void setUp() {
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
                new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("test")),
                CoreAuthenticationTestUtils.getPrincipal(),
                new SimpleTestUsernamePasswordAuthenticationHandler()).getId());
    }

}
