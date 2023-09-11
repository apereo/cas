package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSurrogateAuthenticationPrincipalBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Impersonation")
class DefaultSurrogateAuthenticationPrincipalBuilderTests {
    @Test
    void verifyOperationWithNoService() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val p = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", CoreAuthenticationTestUtils.getPrincipal());
        assertNotNull(p);
    }

    @Test
    void verifyOperationWithService() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());
        val p = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", CoreAuthenticationTestUtils.getPrincipal(), registeredService);
        assertNotNull(p);
    }

    @Test
    void verifyOperationWithoutAuthn() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val resultBuilder = new DefaultAuthenticationResultBuilder();
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertTrue(surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService).isEmpty());
    }


    @Test
    void verifyOperationWithSurrogate() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());

        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate",
            CoreAuthenticationTestUtils.getPrincipal("unknown"), registeredService);

        val resultBuilder = new DefaultAuthenticationResultBuilder();
        resultBuilder.collect(CoreAuthenticationTestUtils.getAuthentication(principal));

        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService));

        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(UUID.randomUUID().toString()));
        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService));
    }

    @Test
    void verifyOperationWithSurrogateSuccess() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());

        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate",
            CoreAuthenticationTestUtils.getPrincipal("test"), registeredService);

        val resultBuilder = new DefaultAuthenticationResultBuilder();
        resultBuilder.collect(CoreAuthenticationTestUtils.getAuthentication(principal));

        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService));

        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        assertTrue(surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService).isPresent());
    }
}
