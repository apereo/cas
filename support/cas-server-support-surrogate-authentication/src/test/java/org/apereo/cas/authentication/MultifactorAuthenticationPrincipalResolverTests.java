package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Authentication")
class MultifactorAuthenticationPrincipalResolverTests {
    @Autowired
    @Qualifier("surrogateMultifactorAuthenticationPrincipalResolver")
    private MultifactorAuthenticationPrincipalResolver surrogateMultifactorAuthenticationPrincipalResolver;

    @Test
    void verifyOperation() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val primary = CoreAuthenticationTestUtils.getPrincipal();
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", primary);
        assertEquals(0, surrogateMultifactorAuthenticationPrincipalResolver.getOrder());
        assertTrue(surrogateMultifactorAuthenticationPrincipalResolver.supports(principal));
        val resolved = surrogateMultifactorAuthenticationPrincipalResolver.resolve(principal);
        assertEquals(primary, resolved);
    }

    @Test
    void verifyDefaultOperation() throws Throwable {
        val resolver = MultifactorAuthenticationPrincipalResolver.identical();
        assertEquals(Ordered.LOWEST_PRECEDENCE, resolver.getOrder());

        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(principal.getAttributes()).thenReturn(Map.of());
        assertTrue(resolver.supports(principal));
        assertEquals(principal, resolver.resolve(principal));
    }
}
