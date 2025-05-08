package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.test=surrogate")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultSurrogateAuthenticationPrincipalBuilderTests {
    @Autowired
    @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME)
    private SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder;

    @Autowired
    @Qualifier(PrincipalElectionStrategy.BEAN_NAME)
    private PrincipalElectionStrategy principalElectionStrategy;
    
    @Test
    void verifyOperationWithNoService() throws Throwable {
        val surrogate = new BasicIdentifiableCredential();
        surrogate.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("surrogate"));
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogate, CoreAuthenticationTestUtils.getPrincipal());
        assertNotNull(principal);
    }

    @Test
    void verifyOperationWithService() throws Throwable {
        val surrogate = new BasicIdentifiableCredential();
        surrogate.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("surrogate"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogate,
            CoreAuthenticationTestUtils.getPrincipal(), registeredService);
        assertNotNull(principal);
    }

    @Test
    void verifyOperationWithoutAuthn() throws Throwable {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val resultBuilder = new DefaultAuthenticationResultBuilder(principalElectionStrategy);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertTrue(surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService).isEmpty());
    }

    @Test
    void verifyOperationWithSurrogate() throws Throwable {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());

        val surrogate = new BasicIdentifiableCredential();
        surrogate.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("surrogate"));
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogate,
            CoreAuthenticationTestUtils.getPrincipal("unknown"), registeredService);

        val resultBuilder = new DefaultAuthenticationResultBuilder(principalElectionStrategy);
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
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());

        val surrogate = new BasicIdentifiableCredential();
        surrogate.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("surrogate"));
        val principal = surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogate,
            CoreAuthenticationTestUtils.getPrincipal("test"), registeredService);

        val resultBuilder = new DefaultAuthenticationResultBuilder(principalElectionStrategy);
        resultBuilder.collect(CoreAuthenticationTestUtils.getAuthentication(principal));

        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(resultBuilder, credential, registeredService));

        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        val builder = surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(
            resultBuilder, credential, registeredService).orElseThrow();
        val authentication = builder.build().getAuthentication();
        assertTrue(authentication.getSingleValuedAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, Boolean.class));
        assertEquals("test", authentication.getSingleValuedAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, String.class));
        assertEquals("surrogate", authentication.getSingleValuedAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, String.class));
    }
}
