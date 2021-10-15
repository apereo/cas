package org.apereo.cas.authentication;

import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
@Tag("Authentication")
public class SurrogateAuthenticationPostProcessorTests {
    @Autowired
    @Qualifier("surrogateAuthenticationPostProcessor")
    private AuthenticationPostProcessor surrogateAuthenticationPostProcessor;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    public void verifySupports() {
        assertFalse(surrogateAuthenticationPostProcessor.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(surrogateAuthenticationPostProcessor.supports(new SurrogateUsernamePasswordCredential()));
    }

    @Test
    public void verifySurrogateCredentialNotFound() {
        val c = new SurrogateUsernamePasswordCredential();
        c.setUsername("casuser");
        c.setPassword("Mellon");
        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("service"), c);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }

    @Test
    public void verifyProcessorWorks() {
        val c = new SurrogateUsernamePasswordCredential();
        c.setUsername("casuser");
        c.setPassword("Mellon");
        c.setSurrogateUsername("cassurrogate");
        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("https://localhost"), c);
        val builder = mock(AuthenticationBuilder.class);
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser"));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                surrogateAuthenticationPostProcessor.process(builder, transaction);
            }
        });
    }

    @Test
    public void verifyNoPrimaryCredential() {
        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(
            RegisteredServiceTestUtils.getService("service"), new Credential[0]);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }

    @Test
    public void verifyAuthN() {
        val c = new SurrogateUsernamePasswordCredential();
        c.setUsername("casuser");
        c.setPassword("Mellon");
        c.setSurrogateUsername("cassurrogate");
        val service = RegisteredServiceTestUtils.getService("service");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));

        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(service, c);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                surrogateAuthenticationPostProcessor.process(builder, transaction);
            }
        });
    }

    @Test
    public void verifyFailAuthN() {
        val c = new SurrogateUsernamePasswordCredential();
        c.setUsername("casuser");
        c.setPassword("Mellon");
        c.setSurrogateUsername("other-user");
        val service = RegisteredServiceTestUtils.getService("service");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));

        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(service, c);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }
}
