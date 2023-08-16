package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * This is {@link OidcWebFingerDiscoveryService}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface OidcWebFingerDiscoveryService {
    /**
     * Handle request.
     *
     * @param resource the resource
     * @param rel      the rel
     * @return the response entity
     * @throws Throwable the throwable
     */
    ResponseEntity<Map> handleRequest(String resource, String rel) throws Throwable;

    /**
     * Gets discovery settings.
     *
     * @return the discovery
     */
    OidcServerDiscoverySettings getDiscovery();
}
