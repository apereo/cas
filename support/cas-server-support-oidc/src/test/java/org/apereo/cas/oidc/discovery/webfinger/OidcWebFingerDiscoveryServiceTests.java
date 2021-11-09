package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcWebFingerDiscoveryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcWebFingerDiscoveryServiceTests extends AbstractOidcTests {

    @Test
    public void verifyNotFound() {
        val entity = oidcWebFingerDiscoveryService.handleWebFingerDiscoveryRequest("resource", OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCodeValue());
    }

    @Test
    public void verifyAccountMismatch() {
        val entity = oidcWebFingerDiscoveryService.handleWebFingerDiscoveryRequest(
            "okta:acct:joe.stormtrooper@example.com", "whatever");
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCodeValue());
    }

    @Test
    public void verifyAccount() {
        val entity = oidcWebFingerDiscoveryService.handleWebFingerDiscoveryRequest(
            "okta:acct:joe.stormtrooper@sso.example.org", OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_OK, entity.getStatusCodeValue());
    }

    @Test
    public void verifyNoAccount() {
        val repository = mock(OidcWebFingerUserInfoRepository.class);
        when(repository.findByEmailAddress(anyString())).thenReturn(Map.of());
        when(repository.findByUsername(anyString())).thenReturn(Map.of());
        val service = new OidcWebFingerDiscoveryService(repository,
            new OidcServerDiscoverySettings("https://apereo.org/cas"));
        val entity = service.handleWebFingerDiscoveryRequest(
            "okta:acct:joe.stormtrooper@sso.example.org", OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCodeValue());
    }

    @Test
    public void verifyMismatchResource() {
        val entity = oidcWebFingerDiscoveryService.handleWebFingerDiscoveryRequest(
            StringUtils.EMPTY, OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCodeValue());
    }
}
