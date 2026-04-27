package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole.isTaOrIntermediate;

/**
 * This is {@link OidcWellKnownFederationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class OidcWellKnownFederationEndpointController extends AbstractOidcFederationEndpointController {

    private final ObjectProvider<OidcServerDiscoverySettings> serverDiscoverySettings;

    public OidcWellKnownFederationEndpointController(final ObjectProvider<OidcServerDiscoverySettings> serverDiscoverySetting,
                                                      final OidcIssuerService oidcIssuerService,
                                                      final OidcFederationEntityStatementService federationEntityStatementService,
                                                      final OidcProperties oidcProperties) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.serverDiscoverySettings = serverDiscoverySetting;
    }

    /**
     * Gets well known discovery federation configuration.
     *
     * @param request  the request
     * @param response the response
     * @return the well known discovery configuration
     */
    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL,
        "/**/" + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL
    })
    @Operation(summary = "Handle OIDC discovery federation request",
        description = "Handles requests for well-known OIDC discovery federation configuration")
    public ResponseEntity getWellKnownDiscoveryConfiguration(
        final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        val error = retrieveInvalidIssuerError(request, response, OidcConstants.FETCH_FEDERATION_URL);
        if (error != null) {
            return error;
        }

        val role = oidcProperties.getFederation().getRole();
        val settings = serverDiscoverySettings.getIfAvailable();
        var issuer = oidcProperties.getCore().getIssuer();
        val metadata = new JSONObject();
        var authorityHints = (List<EntityID>) null;
        if (settings != null) {
            if (role != OidcFederationRole.OPENID_PROVIDER) {
                throw new IllegalArgumentException("Federation role [" + role + "] is not supported for OpenID Provider");
            }
            issuer = settings.getIssuer();

            authorityHints = oidcProperties.getFederation().getAuthorityHints().stream().map(EntityID::new).toList();

            val json = JSONValue.parse(settings.toJson());
            metadata.put(EntityType.OPENID_PROVIDER.getValue(), json);
        } else if (!isTaOrIntermediate(role)) {
            throw new IllegalArgumentException("Federation role [" + role + "] is not supported for Trust Anchor/Intermediate");
        }

        val federationMetadata = buildMetadata(issuer);
        metadata.put(EntityType.FEDERATION_ENTITY.getValue(), federationMetadata.toJSONObject());

        return buildEntityStatement(issuer, issuer, metadata, null, authorityHints);
    }
}
