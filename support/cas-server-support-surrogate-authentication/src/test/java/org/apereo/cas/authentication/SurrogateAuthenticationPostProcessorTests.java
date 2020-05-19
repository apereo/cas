package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationAuditConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationRestConfiguration;
import org.apereo.cas.config.SurrogateComponentSerializationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    SurrogateAuthenticationConfiguration.class,
    SurrogateComponentSerializationConfiguration.class,
    SurrogateAuthenticationRestConfiguration.class,
    SurrogateAuthenticationAuditConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class
},
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Simple")
public class SurrogateAuthenticationPostProcessorTests {
    @Autowired
    @Qualifier("surrogateAuthenticationPostProcessor")
    private AuthenticationPostProcessor surrogateAuthenticationPostProcessor;

    @Autowired
    @Qualifier("servicesManager")
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
        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("service"), c);
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
        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("https://localhost"), c);
        val builder = mock(AuthenticationBuilder.class);
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser"));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                surrogateAuthenticationPostProcessor.process(builder, transaction);
            }
        });
    }

    @Test
    public void verifyNoPrimaryCredential() {
        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("service"), new Credential[0]);
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

        val transaction = DefaultAuthenticationTransaction.of(service, c);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
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

        val transaction = DefaultAuthenticationTransaction.of(service, c);
        val builder = mock(AuthenticationBuilder.class);
        val principal = new SurrogatePrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getPrincipal("something"));
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal));
        assertThrows(AuthenticationException.class, () -> surrogateAuthenticationPostProcessor.process(builder, transaction));
    }
}
