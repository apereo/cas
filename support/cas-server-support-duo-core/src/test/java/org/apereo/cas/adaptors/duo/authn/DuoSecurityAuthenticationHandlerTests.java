package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.DirectObjectProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("DuoSecurity")
public class DuoSecurityAuthenticationHandlerTests {

    @Test
    public void verifyDuoSecurityPasscode() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);

        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.matches(anyString())).thenReturn(true);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);

        val credential = new DuoSecurityPasscodeCredential(authentication.getPrincipal().getId(), "645341",
            DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertTrue(handler.supports(credential));

        when(duoService.authenticate(any(Credential.class)))
            .thenReturn(DuoSecurityAuthenticationResult.builder()
                .success(true).username(authentication.getPrincipal().getId()).build());
        var result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);

        when(duoService.authenticate(any(Credential.class))).thenThrow(new RuntimeException("Unable to authenticate"));
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    private static DuoSecurityAuthenticationHandler getAuthenticationHandler(
        final DuoSecurityMultifactorAuthenticationProvider provider) {
        return new DuoSecurityAuthenticationHandler(null,
            mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory(),
            new DirectObjectProvider<>(provider), 0, List.of(MultifactorAuthenticationPrincipalResolver.identical()));
    }


    @Test
    public void verifyDuoSecurityUniversalPromptCredential() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(UniversalPromptDuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class)))
            .thenReturn(DuoSecurityAuthenticationResult.builder()
                .success(true).username(authentication.getPrincipal().getId()).build());
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);
        when(provider.matches(anyString())).thenReturn(true);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityUniversalPromptCredential("token", authentication);
        credential.setProviderId(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertTrue(handler.supports(credential));

        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
    }

    @Test
    public void verifyDuoSecurityUniversalPromptCredentialFails() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(UniversalPromptDuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class)))
            .thenThrow(new FailedLoginException());
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityUniversalPromptCredential("token", authentication);
        credential.setProviderId(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyDuoSecurityDirectCredential() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class)))
            .thenReturn(DuoSecurityAuthenticationResult.builder()
                .success(true).username(authentication.getPrincipal().getId()).build());
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityDirectCredential(authentication.getPrincipal(),
            DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
    }

    @Test
    public void verifyDuoSecurityCredential() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class)))
            .thenReturn(DuoSecurityAuthenticationResult.builder()
                .success(true).username(authentication.getPrincipal().getId()).build());
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityCredential(authentication.getPrincipal().getId(),
            authentication.getPrincipal().getId(), DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
    }

    @Test
    public void verifyDuoSecurityCredentialAuthnFails() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class))).thenThrow(FailedLoginException.class);
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityCredential(authentication.getPrincipal().getId(),
            authentication.getPrincipal().getId(), DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }


    @Test
    public void verifyBadDuoSecurityCredential() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class)))
            .thenReturn(DuoSecurityAuthenticationResult.builder()
                .success(true).username(authentication.getPrincipal().getId()).build());
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityCredential(null, null, DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyDirectDuoSecurityCredential() throws Exception {
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class))).thenThrow(FailedLoginException.class);
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityDirectCredential(CoreAuthenticationTestUtils.getAuthentication().getPrincipal(), DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifySupports() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);

        when(duoService.authenticate(any(Credential.class)))
            .thenReturn(DuoSecurityAuthenticationResult.builder().success(true)
                .username(authentication.getPrincipal().getId()).build());
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);
        when(provider.matches(anyString())).thenReturn(true);

        val handler = getAuthenticationHandler(provider);
        val credential = new DuoSecurityCredential("casuser",
            null, DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertTrue(handler.supports(credential));
        assertFalse(handler.supports(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));
    }
}
