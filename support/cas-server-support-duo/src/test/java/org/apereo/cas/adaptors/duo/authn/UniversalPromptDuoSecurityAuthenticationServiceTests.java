package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.Client;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UniversalPromptDuoSecurityAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class UniversalPromptDuoSecurityAuthenticationServiceTests {

    @Test
    public void verifyPingFails() throws Exception {
        val duoClient = mock(Client.class);
        when(duoClient.healthCheck()).thenThrow(new RuntimeException());

        val duoProperties = new DuoSecurityMultifactorAuthenticationProperties();
        val service = new UniversalPromptDuoSecurityAuthenticationService(duoProperties, mock(HttpClient.class), duoClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        assertTrue(service.getDuoClient().isPresent());
        assertFalse(service.ping());
    }

    @Test
    public void verifyPing() throws Exception {
        val duoClient = mock(Client.class);
        when(duoClient.healthCheck()).thenReturn(new HealthCheckResponse());
        val duoProperties = new DuoSecurityMultifactorAuthenticationProperties();
        val service = new UniversalPromptDuoSecurityAuthenticationService(duoProperties, mock(HttpClient.class), duoClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        assertTrue(service.getDuoClient().isPresent());
        assertTrue(service.ping());
    }

    @Test
    public void verifyAuth() throws Exception {
        val state = UUID.randomUUID().toString();
        val credential = new DuoSecurityUniversalPromptCredential(state,
            RegisteredServiceTestUtils.getAuthentication("casuser"));

        val duoClient = mock(Client.class);
        val token = new Token();
        token.setAud("aud");
        token.setIat(123456D);
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

        when(duoClient.exchangeAuthorizationCodeFor2FAResult(anyString(), anyString())).thenReturn(token);

        val duoProperties = new DuoSecurityMultifactorAuthenticationProperties();
        val service = new UniversalPromptDuoSecurityAuthenticationService(duoProperties,
            mock(HttpClient.class), duoClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build());
        val result = service.authenticate(credential);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("CAS", result.getUsername());
        assertNotNull(result.getAttributes());
    }

}
