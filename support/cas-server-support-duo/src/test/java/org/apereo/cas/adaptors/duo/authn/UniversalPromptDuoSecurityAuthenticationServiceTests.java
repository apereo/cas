package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.http.HttpClient;
import com.duosecurity.Client;
import com.duosecurity.exception.DuoException;
import com.duosecurity.model.AccessDevice;
import com.duosecurity.model.Application;
import com.duosecurity.model.AuthContext;
import com.duosecurity.model.AuthDevice;
import com.duosecurity.model.AuthResult;
import com.duosecurity.model.HealthCheckResponse;
import com.duosecurity.model.Location;
import com.duosecurity.model.Token;
import com.duosecurity.model.User;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UniversalPromptDuoSecurityAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DuoSecurity")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class UniversalPromptDuoSecurityAuthenticationServiceTests {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyPing(final boolean pass) throws Throwable {
        val duoClient = mock(Client.class);
        if (pass) {
            when(duoClient.healthCheck()).thenReturn(new HealthCheckResponse());
        } else {
            when(duoClient.healthCheck()).thenThrow(new RuntimeException());
        }
        val duoProperties = new DuoSecurityMultifactorAuthenticationProperties();
        val client = mock(DuoSecurityClient.class);
        when(client.build()).thenReturn(duoClient);
        val service = new UniversalPromptDuoSecurityAuthenticationService(duoProperties, mock(HttpClient.class), client,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build(),
            mock(TenantExtractor.class));
        assertNotNull(service.getDuoClient());
        assertEquals(pass, service.ping());
    }

    @ParameterizedTest
    @ValueSource(strings = "email")
    @NullAndEmptySource
    void verifyAuth(final String principalAttribute) throws Throwable {
        val state = UUID.randomUUID().toString();
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            Map.of("email", List.of("cas@example.org")));
        val credential = new DuoSecurityUniversalPromptCredential(state,
            RegisteredServiceTestUtils.getAuthentication(principal));
        val token = buildDuoAuthenticationToken();
        val duoProperties = new DuoSecurityMultifactorAuthenticationProperties()
            .setPrincipalAttribute(principalAttribute);
        val service = buildAuthenticationService(token, duoProperties);
        val result = service.authenticate(credential);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("CAS", result.getUsername());
        assertNotNull(result.getAttributes());
    }

    private static UniversalPromptDuoSecurityAuthenticationService buildAuthenticationService(
        final Token token, final DuoSecurityMultifactorAuthenticationProperties duoProperties) throws DuoException {
        val duoClient = mock(Client.class);
        when(duoClient.exchangeAuthorizationCodeFor2FAResult(anyString(), anyString())).thenReturn(token);
        val client = mock(DuoSecurityClient.class);
        when(client.build()).thenReturn(duoClient);
        return new UniversalPromptDuoSecurityAuthenticationService(duoProperties,
            mock(HttpClient.class), client, List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build(), mock(TenantExtractor.class));
    }

    private static Token buildDuoAuthenticationToken() {
        val token = new Token();
        token.setAud("aud");
        token.setIat(123456.00D);
        token.setExp(123456);
        token.setAuth_time(123456789);
        token.setIss("issuer");
        token.setSub("casuser");
        token.setPreferred_username("CAS");
        val authContext = new AuthContext();
        val accessDevice = new AccessDevice();
        accessDevice.setLocation(new Location());
        accessDevice.setHostname("hostname");
        authContext.setAccess_device(accessDevice);
        val authDevice = new AuthDevice();
        authDevice.setLocation(new Location());
        authContext.setAuth_device(authDevice);
        authContext.setUser(new User());
        authContext.setApplication(new Application());
        token.setAuth_context(authContext);
        token.setAuth_result(new AuthResult());
        return token;
    }

}
