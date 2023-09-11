package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Impersonation")
class SurrogateAuthenticationPostProcessorTests {
    @Autowired
    @Qualifier("surrogateAuthenticationPostProcessor")
    private AuthenticationPostProcessor surrogateAuthenticationPostProcessor;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifySupports() throws Throwable {
        assertFalse(surrogateAuthenticationPostProcessor.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("something"));
        assertTrue(surrogateAuthenticationPostProcessor.supports(credential));
    }

    @Test
    void verifySurrogateCredentialNotFound() throws Throwable {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("Mellon");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("service"), credential);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }

    @Test
    void verifyProcessorWorks() throws Throwable {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("Mellon");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("https://localhost"), credential);
        val builder = mock(AuthenticationBuilder.class);
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser"));
        assertDoesNotThrow(() -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }

    @Test
    void verifyNoPrimaryCredential() throws Throwable {
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(
            RegisteredServiceTestUtils.getService("service"));
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }

    @Test
    void verifyAuthN() throws Throwable {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("Mellon");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        val service = RegisteredServiceTestUtils.getService("service");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(service, credential);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertDoesNotThrow(() -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }

    @Test
    void verifyFailAuthN() throws Throwable {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("Mellon");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("other-use"));
        val service = RegisteredServiceTestUtils.getService("service");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(service, credential);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }
}
