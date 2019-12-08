package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalResolutionExecutionPlan;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogatePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogatePrincipalResolverTests {
    @Test
    public void verifySupports() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertFalse(resolver.supports(upc));

        val credential = new SurrogateUsernamePasswordCredential();
        credential.setSurrogateUsername("surrogate");
        credential.setUsername("username");
        assertTrue(resolver.supports(credential));
    }

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
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(credential));
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

    @Test
    public void verifyPrincipalResolutionPlan() {
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        val surrogateCreds = new SurrogateUsernamePasswordCredential();
        surrogateCreds.setSurrogateUsername("surrogate");
        surrogateCreds.setUsername(upc.getUsername());

        val plan = new DefaultPrincipalResolutionExecutionPlan();
        plan.registerPrincipalResolver(new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository()));
        plan.registerPrincipalResolver(new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository()));

        val resolver = new ChainingPrincipalResolver();
        resolver.setChain(plan.getRegisteredPrincipalResolvers());

        val upcPrincipal = resolver.resolve(upc, Optional.of(CoreAuthenticationTestUtils.getPrincipal("test")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(upcPrincipal);
        assertEquals(1, upcPrincipal.getAttributes().get("givenName").size());
        assertEquals(upc.getId(), upcPrincipal.getId());

        val surrogatePrincipal = resolver.resolve(surrogateCreds, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(surrogatePrincipal);
        assertEquals(1, surrogatePrincipal.getAttributes().get("givenName").size());
        assertEquals(surrogateCreds.getId(), surrogatePrincipal.getId());
    }
}
