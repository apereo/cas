package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * The federation historical keys an entity publishes (OpenID Federation 1.0, section 8.7): keys it no longer signs
 * with, carrying {@code exp} once retired and {@code revoked} once revoked. They keep statements signed before a key
 * rollover verifiable, unless the key was revoked. The signed key set is fetched from the endpoint the entity
 * configuration advertises, must be typed {@code jwk-set+jwt}, issued by the entity, and verify with the given keys.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class OidcFederationHistoricalKeys {
    private static final String HISTORICAL_KEYS_ENDPOINT = "federation_historical_keys_endpoint";

    private static final JOSEObjectType HISTORICAL_KEYS_TYPE = new JOSEObjectType("jwk-set+jwt");

    private static final String KEYS = "keys";

    private static final int TIMEOUT_MILLIS = 5000;

    /**
     * Fetch the historical keys of an entity.
     *
     * @param configuration    the entity configuration of the entity, which advertises the endpoint
     * @param verificationKeys the keys of the entity to verify the signed key set with
     * @return the historical keys, or an empty list when the entity advertises no historical keys endpoint
     * @throws Exception when the keys cannot be obtained or verified
     */
    public static List<JWK> fetch(final EntityStatement configuration, final JWKSet verificationKeys) throws Exception {
        val federationEntity = configuration.getClaimsSet().getMetadata(EntityType.FEDERATION_ENTITY);
        val endpoint = federationEntity == null ? null : federationEntity.get(HISTORICAL_KEYS_ENDPOINT);
        if (endpoint == null) {
            return List.of();
        }
        val request = new HTTPRequest(HTTPRequest.Method.GET, new URI(endpoint.toString()));
        request.setConnectTimeout(TIMEOUT_MILLIS);
        request.setReadTimeout(TIMEOUT_MILLIS);
        val response = request.send();
        if (!response.indicatesSuccess()) {
            throw new IllegalStateException("Historical keys endpoint " + endpoint + " answered " + response.getStatusCode());
        }
        val processor = new DefaultJWTProcessor<SecurityContext>();
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(HISTORICAL_KEYS_TYPE));
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(Set.copyOf(JWSAlgorithm.Family.SIGNATURE),
            new ImmutableJWKSet<>(verificationKeys)));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
            new JWTClaimsSet.Builder().issuer(configuration.getEntityID().getValue()).build(),
            Set.of(JWTClaimNames.ISSUER, JWTClaimNames.ISSUED_AT, KEYS)));
        val claims = processor.process(response.getBody(), null);
        return JWKSet.parse(Map.<String, Object>of(KEYS, claims.getListClaim(KEYS))).getKeys();
    }

    /**
     * Whether the historical key has been revoked by now.
     *
     * @param key the historical key
     * @return true or false
     */
    public static boolean isRevoked(final JWK key) {
        return key.getKeyRevocation() != null && !key.getKeyRevocation().getRevocationTime().after(new Date());
    }

    /**
     * Whether a statement issued at the given time may have been signed with the historical key: the key was never
     * revoked, and the statement was issued before the key was retired.
     *
     * @param key      the historical key
     * @param issuedAt the time the statement was issued
     * @return true or false
     */
    public static boolean mayHaveSigned(final JWK key, @Nullable final Date issuedAt) {
        return key.getKeyRevocation() == null
            && issuedAt != null
            && (key.getExpirationTime() == null || !issuedAt.after(key.getExpirationTime()));
    }
}
