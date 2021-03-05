package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogatePrincipalBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class SurrogatePrincipalBuilderTests {
    @Test
    public void verifyOperationWithNoService() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val p = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", CoreAuthenticationTestUtils.getPrincipal());
        assertNotNull(p);
    }

    @Test
    public void verifyOperationWithService() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());
        val p = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", CoreAuthenticationTestUtils.getPrincipal(), registeredService);
        assertNotNull(p);
    }

    @Test
    public void verifyOperationWithSurrogate() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());

        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate",
            CoreAuthenticationTestUtils.getPrincipal("unknown"), registeredService);

        val resultBuilder = new DefaultAuthenticationResultBuilder();
        resultBuilder.collect(CoreAuthenticationTestUtils.getAuthentication(principal));

        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(
                resultBuilder, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                "surrogate", registeredService));
    }
}
