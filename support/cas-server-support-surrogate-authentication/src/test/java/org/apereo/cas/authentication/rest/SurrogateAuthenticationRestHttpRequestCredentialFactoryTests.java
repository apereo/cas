package org.apereo.cas.authentication.rest;

import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthenticationRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Impersonation")
class SurrogateAuthenticationRestHttpRequestCredentialFactoryTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;
    
    @Test
    void verifyUnAuthz() {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        request.addHeader(SurrogateAuthenticationRestHttpRequestCredentialFactory.REQUEST_HEADER_SURROGATE_PRINCIPAL, "surrogate");
        requestBody.add("username", "test");
        requestBody.add("password", "password");

        val service = getSurrogateAuthenticationService(Map.of("test", List.of("other-user")));
        val factory = new SurrogateAuthenticationRestHttpRequestCredentialFactory(service, casProperties.getAuthn().getSurrogate());
        assertThrows(SurrogateAuthenticationException.class, () -> factory.fromRequest(request, requestBody));
    }

    @Test
    void verifyOperationByHeader() throws Throwable {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        request.addHeader(SurrogateAuthenticationRestHttpRequestCredentialFactory.REQUEST_HEADER_SURROGATE_PRINCIPAL, "surrogate");
        requestBody.add("username", "test");
        requestBody.add("password", "password");

        val service = getSurrogateAuthenticationService(Map.of("test", List.of("surrogate")));
        val factory = new SurrogateAuthenticationRestHttpRequestCredentialFactory(service, casProperties.getAuthn().getSurrogate());
        assertTrue(factory.getOrder() > 0);
        val results = factory.fromRequest(request, requestBody);
        assertFalse(results.isEmpty());
        val credential = results.getFirst();
        assertNotNull(credential);
        assertEquals("surrogate", credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).get().getSurrogateUsername());
        assertEquals("test", credential.getId());
    }

    @Test
    void verifyEmptyCreds() throws Throwable {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        val service = getSurrogateAuthenticationService(Map.of("test", List.of("surrogate")));
        val factory = new SurrogateAuthenticationRestHttpRequestCredentialFactory(service, casProperties.getAuthn().getSurrogate());
        assertTrue(factory.fromRequest(request, requestBody).isEmpty());
    }

    @Test
    void verifyOperationByCredentialSeparator() throws Throwable {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "surrogate+test");
        requestBody.add("password", "password");

        val service = getSurrogateAuthenticationService(Map.of("test", List.of("surrogate")));
        val factory = new SurrogateAuthenticationRestHttpRequestCredentialFactory(service, casProperties.getAuthn().getSurrogate());
        val results = factory.fromRequest(request, requestBody);
        assertFalse(results.isEmpty());
        val credential = results.getFirst();
        assertNotNull(credential);
        assertEquals("surrogate", credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).get().getSurrogateUsername());
        assertEquals("test", credential.getId());
    }

    @Test
    void verifyBasicUsernamePasswordOperationWithoutSurrogatePrincipal() throws Throwable {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "test");
        requestBody.add("password", "password");

        val service = getSurrogateAuthenticationService(Map.of());
        val factory = new SurrogateAuthenticationRestHttpRequestCredentialFactory(service, casProperties.getAuthn().getSurrogate());
        val results = factory.fromRequest(request, requestBody);
        assertFalse(results.isEmpty());
        val credential = results.getFirst();
        assertNotNull(credential);
        assertTrue(credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isEmpty());
        assertEquals("test", credential.getId());
    }

    private SurrogateAuthenticationService getSurrogateAuthenticationService(final Map accounts) {
        return new SimpleSurrogateAuthenticationService(accounts, mock(ServicesManager.class),
            casProperties, principalAccessStrategyEnforcer, applicationContext);
    }

}
