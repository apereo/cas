package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringBootTest(
    classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Impersonation")
@ExtendWith(CasTestExtension.class)
class MultifactorAuthenticationPrincipalResolverTests {
    @Autowired
    @Qualifier("surrogateMultifactorAuthenticationPrincipalResolver")
    private MultifactorAuthenticationPrincipalResolver surrogateMultifactorAuthenticationPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val surrogatePrincipalBuilder = getBuilder();
        val primary = CoreAuthenticationTestUtils.getPrincipal();
        val surrogate = new BasicIdentifiableCredential();
        surrogate.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("surrogate"));
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogate, primary);
        assertEquals(0, surrogateMultifactorAuthenticationPrincipalResolver.getOrder());
        assertTrue(surrogateMultifactorAuthenticationPrincipalResolver.supports(principal));
        val resolved = surrogateMultifactorAuthenticationPrincipalResolver.resolve(principal);
        assertEquals(primary, resolved);
    }

    @Test
    void verifyDefaultOperation() {
        val resolver = MultifactorAuthenticationPrincipalResolver.identical();
        assertEquals(Ordered.LOWEST_PRECEDENCE, resolver.getOrder());

        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(principal.getAttributes()).thenReturn(Map.of());
        assertTrue(resolver.supports(principal));
        assertEquals(principal, resolver.resolve(principal));
    }

    private SurrogateAuthenticationPrincipalBuilder getBuilder() {
        val surrogateAuthenticationService = new SimpleSurrogateAuthenticationService(
            Map.of("test", List.of("surrogate")),
            servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        return new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            surrogateAuthenticationService,
            attributeRepositoryResolver,
            casProperties);
    }
}
