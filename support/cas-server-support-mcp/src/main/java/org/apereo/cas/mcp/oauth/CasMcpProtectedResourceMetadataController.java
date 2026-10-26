package org.apereo.cas.mcp.oauth;

import module java.base;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link CasMcpProtectedResourceMetadataController}, which publishes the OAuth protected resource metadata
 * (RFC 9728) of the MCP server: the resource identifier, CAS as the authorization server through its OpenID Connect
 * issuer, the supported scopes and how tokens are presented. The document is served under the CAS context path, at the
 * well-known path followed by the MCP endpoint path, and clients find it through the {@code resource_metadata} parameter
 * of the challenges.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RestController("casMcpProtectedResourceMetadataController")
@RequiredArgsConstructor
public class CasMcpProtectedResourceMetadataController {
    private final CasMcpProtectedResource protectedResource;

    private final ObjectProvider<OidcIssuerService> oidcIssuerService;

    private final String resourceName;

    /**
     * Protected resource metadata.
     *
     * @return the metadata document
     */
    @GetMapping(path = CasMcpProtectedResource.WELL_KNOWN_PATH + "${spring.ai.mcp.server.streamable-http.mcp-endpoint:/mcp}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getProtectedResourceMetadata() {
        val metadata = new LinkedHashMap<String, Object>();
        metadata.put("resource", protectedResource.resource());
        metadata.put("authorization_servers", List.of(oidcIssuerService.getObject().determineIssuer(Optional.empty())));
        metadata.put("scopes_supported", List.of(CasMcpProtectedResource.SCOPE_READ));
        metadata.put("bearer_methods_supported", List.of("header"));
        metadata.put("resource_name", resourceName);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(metadata);
    }
}
