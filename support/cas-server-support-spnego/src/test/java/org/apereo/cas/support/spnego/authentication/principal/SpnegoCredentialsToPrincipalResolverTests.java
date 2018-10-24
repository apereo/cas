package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 */
public class SpnegoCredentialsToPrincipalResolverTests {
    private SpnegoPrincipalResolver resolver;

    private SpnegoCredential spnegoCredentials;

    @BeforeEach
    public void initialize() {
        this.resolver = new SpnegoPrincipalResolver();
        this.spnegoCredentials = new SpnegoCredential(new byte[]{0, 1, 2});
    }

    @Test
    public void verifyValidCredentials() {
        this.spnegoCredentials.setPrincipal(new DefaultPrincipalFactory().createPrincipal("test"));
        assertEquals("test", this.resolver.resolve(this.spnegoCredentials,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler())).getId());
    }

    @Test
    public void verifySupports() {
        assertFalse(this.resolver.supports(null));
        assertTrue(this.resolver.supports(this.spnegoCredentials));
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }
}
