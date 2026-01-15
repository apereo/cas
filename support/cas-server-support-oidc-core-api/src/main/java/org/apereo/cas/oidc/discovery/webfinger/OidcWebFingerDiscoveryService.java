package org.apereo.cas.oidc.discovery.webfinger;

import module java.base;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;

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
    ResponseEntity<@NonNull Map> handleRequest(String resource, String rel) throws Throwable;

    /**
     * Gets discovery settings.
     *
     * @return the discovery
     */
    OidcServerDiscoverySettings getDiscovery();
}
