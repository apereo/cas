package org.apereo.cas.token.authentication;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = "cas.authn.token.sso-token-enabled=true")
@Tag("AuthenticationHandler")
@Import(TokenAuthenticationHandlerTests.TokenAuthenticationTestConfiguration.class)
class TokenAuthenticationHandlerTests extends BaseTokenAuthenticationTests {
    private static final RandomStringGenerator RANDOM_STRING_GENERATOR = new DefaultRandomStringGenerator();

    private static final String SIGNING_SECRET = RANDOM_STRING_GENERATOR.getNewString(256);

    private static final String ENCRYPTION_SECRET = RANDOM_STRING_GENERATOR.getNewString(48);

    @Test
    void verifyPostProcessorAuthentication() throws Throwable {
        val service = RegisteredServiceTestUtils.getService();
        val authenticationBuilder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        tokenAuthenticationPostProcessor.process(authenticationBuilder,
            CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(service));
        val authentication = authenticationBuilder.build();
        assertTrue(authentication.containsAttribute(TokenConstants.PARAMETER_NAME_TOKEN));
        val token = authentication.getAttributes().get(TokenConstants.PARAMETER_NAME_TOKEN).getFirst().toString();
        val credential = new TokenCredential(token, service);
        val result = tokenAuthenticationHandler.authenticate(credential, credential.getService());
        assertEquals(result.getPrincipal().getId(), authentication.getPrincipal().getId());
    }

    @Test
    void verifyAuthentication() throws Throwable {
        val service = RegisteredServiceTestUtils.getService();
        val registeredService = servicesManager.findServiceBy(service);
        val generator = TokenAuthenticationSecurity.forRegisteredService(registeredService).toGenerator();
        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());
        val token = generator.generate(profile);
        val credential = new TokenCredential(token, service);
        val result = tokenAuthenticationHandler.authenticate(credential, credential.getService());
        assertEquals(result.getPrincipal().getId(), profile.getId());
    }

    @Test
    void verifyNoService() {
        val service = RegisteredServiceTestUtils.getService("nosigningservice");
        val registeredService = servicesManager.findServiceBy(service);
        assertThrows(UnauthorizedServiceException.class,
            () -> TokenAuthenticationSecurity.forRegisteredService(registeredService).toGenerator());
        val credential = new TokenCredential(UUID.randomUUID().toString(), service);
        assertThrows(AuthenticationException.class, () -> tokenAuthenticationHandler.authenticate(credential, credential.getService()));
    }

    @Test
    void verifyNoSigning() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL2);
        val registeredService = servicesManager.findServiceBy(service);
        val generator = TokenAuthenticationSecurity.forRegisteredService(registeredService).toGenerator();
        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());
        val token = generator.generate(profile);
        val credential = new TokenCredential(token, service);
        val result = tokenAuthenticationHandler.authenticate(credential, credential.getService());
        assertEquals(result.getPrincipal().getId(), profile.getId());
    }

    @Test
    void verifyNoEnc() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL3);
        val registeredService = servicesManager.findServiceBy(service);
        val generator = TokenAuthenticationSecurity.forRegisteredService(registeredService).toGenerator();
        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());
        val token = generator.generate(profile);
        val credential = new TokenCredential(token, service);
        assertNotNull(tokenAuthenticationHandler.authenticate(credential, credential.getService()));
    }

    @TestConfiguration(value = "TokenAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class TokenAuthenticationTestConfiguration {
        @Bean
        public List inMemoryRegisteredServices() {
            val services = new ArrayList<RegisteredService>();

            val svc1 = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL);
            svc1.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
            svc1.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());

            val p = new DefaultRegisteredServiceProperty();
            p.addValue(SIGNING_SECRET);
            svc1.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), p);

            val p2 = new DefaultRegisteredServiceProperty();
            p2.addValue(ENCRYPTION_SECRET);
            svc1.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName(), p2);

            services.add(svc1);

            val svc2 = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL2);
            svc2.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
            services.add(svc2);

            val svc3 = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL3);
            svc3.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
            svc3.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), p);
            services.add(svc3);

            return services;
        }
    }

}
