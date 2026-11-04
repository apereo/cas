package org.apereo.cas.oidc.federation.signature;

import module java.base;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import org.jspecify.annotations.Nullable;

/**
 * Holds the Federation Entity Keys of CAS and signs with them. Keys go through the lifecycle of the OpenID Connect
 * keystore (future, current, previous) and are then kept as historical keys (OpenID Federation 1.0, section 8.7).
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface OidcFederationJsonWebKeystoreService {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcFederationWebKeystoreService";

    /**
     * The keys published in the entity configuration: current, future and previous keys, public only.
     *
     * @return the key set
     * @throws Exception the exception
     */
    JWKSet toJWKSet() throws Exception;

    /**
     * Sign entity statement with the current key, named by {@code kid}.
     *
     * @param claims the claims
     * @return the entity statement
     * @throws Exception the exception
     */
    EntityStatement signEntityStatement(EntityStatementClaimsSet claims) throws Exception;

    /**
     * Sign a JWT with the current key, named by {@code kid}, with the given type.
     *
     * @param claims the claims
     * @param type   the JWT type
     * @return the signed JWT
     * @throws Exception the exception
     */
    SignedJWT sign(JWTClaimsSet claims, JOSEObjectType type) throws Exception;

    /**
     * Keys no longer published in the entity configuration, public only, each with {@code exp} and, when known,
     * {@code iat}.
     *
     * @return the historical keys
     */
    List<JWK> getHistoricalKeys();

    /**
     * Rotate the keys: the future key becomes current, the current key becomes previous, and a new future key is
     * added. When there is no future key yet, one is only added, so that a key never signs before it was published.
     *
     * @return the keys published after the rotation
     * @throws Exception the exception
     */
    JWKSet rotate() throws Exception;

    /**
     * Retire the previous keys: they are no longer published in the entity configuration and are kept as historical
     * keys, without their private keys.
     *
     * @return the keys published after the revocation
     * @throws Exception the exception
     */
    JWKSet revoke() throws Exception;

    /**
     * Revoke a key, such as one whose private key is believed compromised (OpenID Federation 1.0, section 8.7): it is
     * published as a historical key marked {@code revoked}, with the time of the revocation and the reason, if any. A
     * key that was still published leaves the entity configuration at once and expires now; when it was the current
     * key, the future key starts signing, or a new key when there is none.
     *
     * @param keyId  the key identifier
     * @param reason the reason of the revocation, such as {@code compromised}, {@code superseded} or
     *               {@code unspecified}, or null
     * @return the keys published after the revocation, or empty when no such key is known
     * @throws Exception the exception
     */
    Optional<JWKSet> revokeKey(String keyId, @Nullable String reason) throws Exception;
}
