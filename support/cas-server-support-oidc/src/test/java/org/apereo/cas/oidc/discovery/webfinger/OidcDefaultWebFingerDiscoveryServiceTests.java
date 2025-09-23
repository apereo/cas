package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcDefaultWebFingerDiscoveryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
class OidcDefaultWebFingerDiscoveryServiceTests extends AbstractOidcTests {

    @Test
    void verifyNotFound() throws Throwable {
        val entity = oidcWebFingerDiscoveryService.handleRequest("resource", OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCode().value());
    }

    @Test
    void verifyAccountMismatch() throws Throwable {
        val entity = oidcWebFingerDiscoveryService.handleRequest(
            "okta:acct:joe.stormtrooper@example.com", "whatever");
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCode().value());
    }

    @Test
    void verifyAccount() throws Throwable {
        val entity = oidcWebFingerDiscoveryService.handleRequest(
            "okta:acct:joe.stormtrooper@sso.example.org", OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_OK, entity.getStatusCode().value());
    }

    @Test
    void verifyNoAccount() throws Throwable {
        val repository = mock(OidcWebFingerUserInfoRepository.class);
        when(repository.findByEmailAddress(anyString())).thenReturn(Map.of());
        when(repository.findByUsername(anyString())).thenReturn(Map.of());
        val service = new OidcDefaultWebFingerDiscoveryService(repository,
            new OidcServerDiscoverySettings("https://apereo.org/cas"),
            casProperties.getAuthn().getOidc().getWebfinger());
        val entity = service.handleRequest(
            "okta:acct:joe.stormtrooper@sso.example.org", OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCode().value());
    }

    @Test
    void verifyMismatchResource() throws Throwable {
        val entity = oidcWebFingerDiscoveryService.handleRequest(
            StringUtils.EMPTY, OidcConstants.WEBFINGER_REL);
        assertEquals(HttpStatus.SC_NOT_FOUND, entity.getStatusCode().value());
    }
}
