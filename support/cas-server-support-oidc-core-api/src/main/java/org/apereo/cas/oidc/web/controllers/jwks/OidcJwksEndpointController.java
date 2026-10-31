package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotatedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcJwksEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcJwksEndpointController extends BaseOidcController {
    private static final String JSON_WEB_KEYSET_CACHE_KEY = "jwks";

    private static final Duration JSON_WEB_KEYSET_CACHE_EXPIRATION = Duration.ofDays(1);

    private final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService;

    private final Cache<String, PublishedJsonWebKeySet> jsonWebKeySetCache;

    public OidcJwksEndpointController(final OidcConfigurationContext configurationContext,
                                      final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService) {
        super(configurationContext);
        this.oidcJsonWebKeystoreGeneratorService = oidcJsonWebKeystoreGeneratorService;
        this.jsonWebKeySetCache = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(getJsonWebKeySetCacheExpiration(configurationContext))
            .build();
    }

    /*
     * Signing reloads its keys when the keystore cache expires, so the published key set expires no later than that.
     */
    private static Duration getJsonWebKeySetCacheExpiration(final OidcConfigurationContext configurationContext) {
        val signingKeysExpiration = Beans.newDuration(configurationContext.getCasProperties()
            .getAuthn().getOidc().getJwks().getCore().getJwksCacheExpiration());
        return signingKeysExpiration.isNegative() || signingKeysExpiration.compareTo(JSON_WEB_KEYSET_CACHE_EXPIRATION) > 0
            ? JSON_WEB_KEYSET_CACHE_EXPIRATION
            : signingKeysExpiration;
    }

    /**
     * Handle request for jwk set.
     *
     * @param request  the request
     * @param response the response
     * @param kid      the kid
     * @param state    the state
     * @return the jwk set
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL,
        "/**/" + OidcConstants.JWKS_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Produces the collection of keys from the keystore",
        parameters = @Parameter(name = "state", description = "Filter keys by their state name", required = false))
    public ResponseEntity handleRequestInternal(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                @RequestParam(value = "kid", required = false) final String kid,
                                                @RequestParam(value = "state", required = false) final String state) {
        if (!getConfigurationContext().getIssuerService().validateIssuer(request, List.of(OidcConstants.JWKS_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return FunctionUtils.doAndHandle(() -> {
            val body = getPublishedJsonWebKeySet(kid, state);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return new ResponseEntity<>(body, HttpStatus.OK);
        }, e -> {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        }).get();
    }

    /**
     * Drop the published key set once the keystore is known to have changed.
     *
     * @param event the event
     */
    @EventListener
    public void handleOidcJsonWebKeystoreModifiedEvent(final OidcJsonWebKeystoreModifiedEvent event) {
        LOGGER.debug("Detected change in [{}]; the published JSON web keystore will be reloaded", event.getFile());
        jsonWebKeySetCache.invalidateAll();
    }

    /**
     * Drop the published key set once keys are rotated or revoked.
     *
     * @param event the event
     */
    @EventListener
    public void handleOidcJsonWebKeystoreRotatedEvent(final OidcJsonWebKeystoreRotatedEvent event) {
        LOGGER.debug("Keys in the JSON web keystore are rotated or revoked; the published JSON web keystore will be reloaded");
        jsonWebKeySetCache.invalidateAll();
    }

    private String getPublishedJsonWebKeySet(final String kid, final String state) {
        val published = Objects.requireNonNull(jsonWebKeySetCache.get(JSON_WEB_KEYSET_CACHE_KEY, _ -> loadJsonWebKeySet()));
        if (StringUtils.isBlank(kid) && StringUtils.isBlank(state)) {
            return published.body();
        }
        val keys = published.keys()
            .stream()
            .filter(key -> StringUtils.isBlank(kid) || kid.equalsIgnoreCase(key.getKeyId()))
            .filter(key -> StringUtils.isBlank(state) || state.equalsIgnoreCase(
                OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).name()))
            .toList();
        return new JsonWebKeySet(keys).toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
    }

    private PublishedJsonWebKeySet loadJsonWebKeySet() {
        val jsonWebKeySet = FunctionUtils.doUnchecked(
            () -> OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(oidcJsonWebKeystoreGeneratorService.generate()));
        return new PublishedJsonWebKeySet(List.copyOf(jsonWebKeySet.getJsonWebKeys()),
            jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
    }

    private record PublishedJsonWebKeySet(List<JsonWebKey> keys, String body) {
    }
}
