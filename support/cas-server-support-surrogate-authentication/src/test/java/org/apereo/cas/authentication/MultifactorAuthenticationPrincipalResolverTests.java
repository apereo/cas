package org.apereo.cas.authentication;

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
public class MultifactorAuthenticationPrincipalResolverTests {
    @Autowired
    @Qualifier("surrogateMultifactorAuthenticationPrincipalResolver")
    private MultifactorAuthenticationPrincipalResolver surrogateMultifactorAuthenticationPrincipalResolver;

    @Test
    public void verifyOperation() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val primary = CoreAuthenticationTestUtils.getPrincipal();
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", primary);
        assertEquals(0, surrogateMultifactorAuthenticationPrincipalResolver.getOrder());
        assertTrue(surrogateMultifactorAuthenticationPrincipalResolver.supports(principal));
        val resolved = surrogateMultifactorAuthenticationPrincipalResolver.resolve(principal);
        assertEquals(primary, resolved);
    }
}
