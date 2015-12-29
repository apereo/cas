package org.jasig.cas.adaptors.trusted.authentication.handler.support;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Andrew Petro
 * @since 3.0.0
 */
public final class PrincipalBearingCredentialsAuthenticationHandlerTests {

    private final PrincipalBearingCredentialsAuthenticationHandler handler
                = new PrincipalBearingCredentialsAuthenticationHandler();
    /**
     * When the credentials bear a Principal, succeed the authentication.
     */
    @Test
    public void verifyNonNullPrincipal() throws Exception {
        final PrincipalBearingCredential credentials = new PrincipalBearingCredential(
                new DefaultPrincipalFactory().createPrincipal("scott"));
        assertNotNull(this.handler.authenticate(credentials));
    }

    @Test
    public void verifySupports() {
        final PrincipalBearingCredential credentials =
                new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("scott"));
        assertTrue(this.handler.supports(credentials));
        assertFalse(this.handler.supports(new UsernamePasswordCredential()));
    }
}
