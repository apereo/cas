package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationEndpointAuthenticationModes;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcFederationEntityMetadataContributor;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateClientAuthenticator;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcWellKnownFederationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class OidcWellKnownFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final String FEDERATION_HISTORICAL_KEYS_ENDPOINT = "federation_historical_keys_endpoint";

    private static final String FEDERATION_TRUST_MARK_ENDPOINT = "federation_trust_mark_endpoint";

    private static final String FEDERATION_TRUST_MARK_STATUS_ENDPOINT = "federation_trust_mark_status_endpoint";

    private static final String FEDERATION_TRUST_MARK_LIST_ENDPOINT = "federation_trust_mark_list_endpoint";

    private static final String FEDERATION_TRUST_MARK_ENDPOINT_AUTH_METHODS = "federation_trust_mark_endpoint_auth_methods";

    private static final String FEDERATION_RESOLVE_ENDPOINT_AUTH_METHODS = "federation_resolve_endpoint_auth_methods";

    private static final String ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED = "endpoint_auth_signing_alg_values_supported";

    private static final String PRIVATE_KEY_JWT = "private_key_jwt";

    private static final String NO_CLIENT_AUTHENTICATION = "none";

    private final ObjectProvider<OidcServerDiscoverySettings> serverDiscoverySettings;

    private final ObjectProvider<OidcFederationEntityMetadataContributor> metadataContributors;

    public OidcWellKnownFederationEndpointController(final ObjectProvider<OidcServerDiscoverySettings> serverDiscoverySetting,
                                                      final ObjectProvider<OidcFederationEntityMetadataContributor> metadataContributors,
                                                      final OidcIssuerService oidcIssuerService,
                                                      final OidcFederationEntityStatementService federationEntityStatementService,
                                                      final OidcProperties oidcProperties) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.serverDiscoverySettings = serverDiscoverySetting;
        this.metadataContributors = metadataContributors;
    }

    /**
     * Gets well known discovery federation configuration. An OpenID provider also publishes the metadata of the other
     * entity types it plays, as contributed by the modules present, such as {@code openid_credential_issuer}.
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
        final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        LOGGER.info("Generating federation entity statement");

        val error = retrieveInvalidIssuerError(request, response, OidcConstants.FETCH_FEDERATION_URL);
        if (error != null) {
            return error;
        }

        return toResponse(buildEntityConfiguration());
    }

    /**
     * Build and sign the entity configuration of CAS, as the well-known endpoint publishes it.
     *
     * @return the entity configuration
     * @throws Exception the exception
     */
    public EntityStatement buildEntityConfiguration() throws Exception {
        val role = oidcProperties.getFederation().getRole();
        val settings = serverDiscoverySettings.getIfAvailable();
        var issuer = oidcIssuerService.determineIssuer(Optional.empty());
        val metadata = new JSONObject();
        val authorityHints = oidcProperties.getFederation().getAuthorityHints().stream().map(EntityID::new).toList();
        if (settings != null) {
            if (role != OidcFederationRole.OPENID_PROVIDER) {
                throw new IllegalArgumentException("Federation role [" + role + "] is not supported for OpenID Provider");
            }
            issuer = settings.getIssuer();

            if (authorityHints.isEmpty()) {
                throw new IllegalArgumentException("OpenID provider requires authority hint(s)");
            }

            val json = JSONValue.parse(settings.toJson());
            metadata.put(EntityType.OPENID_PROVIDER.getValue(), json);
            addContributedMetadata(issuer, metadata);

        } else if (role == OidcFederationRole.INTERMEDIATE) {
            if (authorityHints.isEmpty()) {
                throw new IllegalArgumentException("Intermediate requires authority hint(s)");
            }

        } else if (role == OidcFederationRole.TRUST_ANCHOR) {
            if (!authorityHints.isEmpty()) {
                throw new IllegalArgumentException("Trust anchor requires no authority hints");
            }

        } else {
            throw new IllegalArgumentException("Federation role [" + role + "] is not supported for Trust Anchor/Intermediate");
        }

        val federationMetadata = buildMetadata(issuer).toJSONObject();
        federationMetadata.put(FEDERATION_HISTORICAL_KEYS_ENDPOINT, issuer + OidcConstants.HISTORICAL_KEYS_FEDERATION_URL);
        if (!oidcProperties.getFederation().getIssuedTrustMarks().isEmpty()) {
            federationMetadata.put(FEDERATION_TRUST_MARK_ENDPOINT, issuer + OidcConstants.TRUST_MARK_FEDERATION_URL);
            federationMetadata.put(FEDERATION_TRUST_MARK_STATUS_ENDPOINT, issuer + OidcConstants.TRUST_MARK_STATUS_FEDERATION_URL);
            federationMetadata.put(FEDERATION_TRUST_MARK_LIST_ENDPOINT, issuer + OidcConstants.TRUST_MARK_LIST_FEDERATION_URL);
            addAuthenticationMethods(federationMetadata, FEDERATION_TRUST_MARK_ENDPOINT_AUTH_METHODS,
                oidcProperties.getFederation().getTrustMarkEndpointAuthentication());
        }
        if (role.isTrustAnchorOrIntermediate()) {
            addAuthenticationMethods(federationMetadata, FEDERATION_RESOLVE_ENDPOINT_AUTH_METHODS,
                oidcProperties.getFederation().getResolveEndpointAuthentication());
        }
        metadata.put(EntityType.FEDERATION_ENTITY.getValue(), federationMetadata);

        return federationEntityStatementService.createAndSign(issuer, issuer, metadata, null, authorityHints, Map.of());
    }

    private static void addAuthenticationMethods(final JSONObject federationMetadata, final String parameter,
                                                 final OidcFederationEndpointAuthenticationModes authentication) {
        if (authentication != OidcFederationEndpointAuthenticationModes.NONE) {
            federationMetadata.put(parameter, authentication == OidcFederationEndpointAuthenticationModes.REQUIRED
                ? List.of(PRIVATE_KEY_JWT)
                : List.of(NO_CLIENT_AUTHENTICATION, PRIVATE_KEY_JWT));
            federationMetadata.put(ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED, OidcFederationSubordinateClientAuthenticator.getSigningAlgorithms());
        }
    }

    private void addContributedMetadata(final String entityId, final JSONObject metadata) {
        metadataContributors.orderedStream()
            .map(contributor -> contributor.contribute(entityId))
            .flatMap(contributed -> contributed.entrySet().stream())
            .filter(entry -> !metadata.containsKey(entry.getKey()) && !EntityType.FEDERATION_ENTITY.getValue().equals(entry.getKey()))
            .forEach(entry -> metadata.put(entry.getKey(), new JSONObject(entry.getValue())));
    }
}
