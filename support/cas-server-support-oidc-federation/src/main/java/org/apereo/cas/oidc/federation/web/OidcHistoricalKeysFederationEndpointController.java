package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The Federation Historical Keys endpoint (OpenID Federation 1.0, section 8.7): the keys CAS no longer publishes in its
 * entity configuration, so that statements it signed with them can still be verified. The response is a JWK set signed
 * with the current Federation Entity Key, typed {@code jwk-set+jwt}, with {@code iss}, {@code iat} and {@code keys}; each
 * key carries {@code kid}, {@code exp}, the time it was retired, and {@code iat}, when known.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcHistoricalKeysFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final JOSEObjectType HISTORICAL_KEYS_JWT_TYPE = new JOSEObjectType("jwk-set+jwt");

    private final OidcFederationJsonWebKeystoreService keystoreService;

    public OidcHistoricalKeysFederationEndpointController(final OidcIssuerService oidcIssuerService,
                                                          final OidcFederationEntityStatementService federationEntityStatementService,
                                                          final OidcProperties oidcProperties,
                                                          final OidcFederationJsonWebKeystoreService keystoreService) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.keystoreService = keystoreService;
    }

    /**
     * Publish the historical keys.
     *
     * @param request  the request
     * @param response the response
     * @return the signed JWK set
     * @throws Exception the exception
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.HISTORICAL_KEYS_FEDERATION_URL)
    @Operation(summary = "Handle OIDC federation historical keys request",
        description = "Publishes the federation keys that are no longer in use")
    public ResponseEntity getHistoricalKeys(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val error = retrieveInvalidIssuerError(request, response, OidcConstants.HISTORICAL_KEYS_FEDERATION_URL);
        if (error != null) {
            return error;
        }
        val keys = keystoreService.getHistoricalKeys().stream().map(JWK::toJSONObject).toList();
        val claims = new JWTClaimsSet.Builder()
            .issuer(oidcIssuerService.determineIssuer(Optional.empty()))
            .issueTime(Date.from(Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)))
            .claim("keys", keys)
            .build();
        val signed = keystoreService.sign(claims, HISTORICAL_KEYS_JWT_TYPE);
        LOGGER.debug("Publishing [{}] historical federation keys", keys.size());
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(OidcConstants.HISTORICAL_KEYS_CONTENT_TYPE)
            .body(signed.serialize());
    }
}
