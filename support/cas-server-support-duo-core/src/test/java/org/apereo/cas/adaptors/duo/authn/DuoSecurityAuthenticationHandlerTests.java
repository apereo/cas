package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class DuoSecurityAuthenticationHandlerTests {

    @Test
    public void verifyDuoSecurityDirectCredential() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class))).thenReturn(Pair.of(Boolean.TRUE, authentication.getPrincipal().getId()));
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = new DuoSecurityAuthenticationHandler(null,
            mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory(),
            provider, 0);
        val credential = new DuoSecurityDirectCredential(authentication, provider.getId());
        val result = handler.authenticate(credential);
        assertNotNull(result);
    }

    @Test
    public void verifyDuoSecurityCredential() throws Exception {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.authenticate(any(Credential.class))).thenReturn(Pair.of(Boolean.TRUE, authentication.getPrincipal().getId()));
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val handler = new DuoSecurityAuthenticationHandler(null,
            mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory(),
            provider, 0);
        val credential = new DuoSecurityCredential(authentication.getPrincipal().getId(),
            authentication.getPrincipal().getId(), provider.getId());
        val result = handler.authenticate(credential);
        assertNotNull(result);
    }
}
