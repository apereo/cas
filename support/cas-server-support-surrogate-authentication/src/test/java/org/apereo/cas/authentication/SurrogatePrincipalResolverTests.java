package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogatePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogatePrincipalResolverTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyResolverDefault() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals(p.getId(), credential.getId());
    }

    @Test
    public void verifyResolverAttribute() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals("TEST", p.getId());
    }

    @Test
    public void verifyResolverSurrogateWithoutPrincipal() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        val credential = new SurrogateUsernamePasswordCredential();
        thrown.expect(IllegalArgumentException.class);
        resolver.resolve(credential);
    }

    @Test
    public void verifyResolverSurrogate() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val credential = new SurrogateUsernamePasswordCredential();
        credential.setSurrogateUsername("surrogate");
        credential.setUsername("username");
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("casuser", p.getId());
    }
}
