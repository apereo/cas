package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalResolutionExecutionPlan;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogatePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(MockitoExtension.class)
@Tag("Simple")
public class SurrogatePrincipalResolverTests {
    @Test
    public void verifySupports() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), surrogatePrincipalBuilder);
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertFalse(resolver.supports(upc));

        val credential = new SurrogateUsernamePasswordCredential();
        credential.setSurrogateUsername("surrogate");
        credential.setUsername("username");
        assertTrue(resolver.supports(credential));
    }

    @Test
    public void verifyResolverDefault() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), surrogatePrincipalBuilder);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals(p.getId(), credential.getId());
    }

    @Test
    public void verifyResolverAttribute() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn", surrogatePrincipalBuilder);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals("TEST", p.getId());
    }

    @Test
    public void verifyResolverSurrogateWithoutPrincipal() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn", surrogatePrincipalBuilder);
        val credential = new SurrogateUsernamePasswordCredential();
        credential.setUsername("something");
        credential.setSurrogateUsername("something2");
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(credential));
    }

    @Test
    public void verifyResolverSurrogate() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), surrogatePrincipalBuilder);
        val credential = new SurrogateUsernamePasswordCredential();
        credential.setSurrogateUsername("surrogate");
        credential.setUsername("username");
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("surrogate", p.getId());
    }

    @Test
    public void verifyPrincipalResolutionPlan() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        val surrogateCreds = new SurrogateUsernamePasswordCredential();
        surrogateCreds.setSurrogateUsername("surrogate");
        surrogateCreds.setUsername(upc.getUsername());

        val plan = new DefaultPrincipalResolutionExecutionPlan();
        plan.registerPrincipalResolver(new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository()));
        plan.registerPrincipalResolver(new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), surrogatePrincipalBuilder));

        val resolver = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy());
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
