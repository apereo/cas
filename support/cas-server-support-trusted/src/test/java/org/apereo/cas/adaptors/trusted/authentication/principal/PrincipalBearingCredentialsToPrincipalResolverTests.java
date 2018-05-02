package org.apereo.cas.adaptors.trusted.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class PrincipalBearingCredentialsToPrincipalResolverTests {
    private PrincipalBearingPrincipalResolver resolver;

    @Before
    public void setUp() {
        this.resolver = new PrincipalBearingPrincipalResolver();
    }

    @Test
    public void verifySupports() {
        final PrincipalBearingCredential credential = new PrincipalBearingCredential(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test"));
        assertTrue(this.resolver.supports(credential));
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
        assertFalse(this.resolver.supports(null));
    }

    @Test
    public void verifyReturnedPrincipal() {
        final PrincipalBearingCredential credential = new PrincipalBearingCredential(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test"));
        final Principal p = this.resolver.resolve(credential,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals("test", p.getId());
    }

}
