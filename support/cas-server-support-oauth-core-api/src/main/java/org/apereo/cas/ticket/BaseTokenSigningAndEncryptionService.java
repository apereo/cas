package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.EncodingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.EllipticCurves;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseTokenSigningAndEncryptionService implements OAuth20TokenSigningAndEncryptionService {

    @FunctionalInterface
    private interface AlgorithmIdentifierMap {
        String getAlgorithmIdentifier(PublicJsonWebKey jsonKey);
    }

    private static final Map<String, AlgorithmIdentifierMap> ALGORITHM_IDENTIFIER_INTERFACE_MAP = new HashMap<>();
    private static final Map<String, String> ALGORITHM_IDENTIFIER_EC_MAP = new HashMap<>();

    static {
        ALGORITHM_IDENTIFIER_EC_MAP.put(EllipticCurves.P_256, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
        ALGORITHM_IDENTIFIER_EC_MAP.put(EllipticCurves.P_384, AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384);
        ALGORITHM_IDENTIFIER_EC_MAP.put(EllipticCurves.P_521, AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);

        ALGORITHM_IDENTIFIER_INTERFACE_MAP.put("RSA", jwk -> AlgorithmIdentifiers.RSA_USING_SHA256);
        ALGORITHM_IDENTIFIER_INTERFACE_MAP.put("EC", jwk -> {
            if (jwk != null && jwk instanceof EllipticCurveJsonWebKey) {
                val curve = ((EllipticCurveJsonWebKey) jwk).getCurveName();
                val algorithmIdentifier = ALGORITHM_IDENTIFIER_EC_MAP.get(curve);
                if (algorithmIdentifier != null) {
                    return algorithmIdentifier;
                }
            }
            throw new IllegalArgumentException("Unsupported JSON key type");
        });
    }

    private final String issuer;

    /**
     * Get JWK signing algorithm.
     *
     * @param jsonKey JSON Web Key
     * @return        Algorithm
     */
    protected String getJsonWebKeySigningAlgorithm(final PublicJsonWebKey jsonKey) {
        val algorithmIdentifiersInterfaceMap = ALGORITHM_IDENTIFIER_INTERFACE_MAP.get(jsonKey.getKeyType());
        if (algorithmIdentifiersInterfaceMap != null) {
            return algorithmIdentifiersInterfaceMap.getAlgorithmIdentifier(jsonKey);
        }
        throw new IllegalArgumentException("Unsupported JSON key type");
    }

    /**
     * Create json web encryption json web encryption.
     *
     * @param encryptionAlg      the encryption alg
     * @param encryptionEncoding the encryption encoding
     * @param keyIdHeaderValue   the key id header value
     * @param publicKey          the public key
     * @param payload            the payload
     * @return the json web encryption
     */
    @SneakyThrows
    protected String encryptToken(final String encryptionAlg,
                                  final String encryptionEncoding,
                                  final String keyIdHeaderValue,
                                  final Key publicKey,
                                  final String payload) {
        return EncodingUtils.encryptValueAsJwt(publicKey, payload, encryptionAlg,
            encryptionEncoding, keyIdHeaderValue, new HashMap<>(0));
    }

    /**
     * Configure json web signature for id token signing.
     *
     * @param svc        the svc
     * @param claims     the claims
     * @param jsonWebKey the json web key
     * @return the json web signature
     */
    protected String signToken(final OAuthRegisteredService svc,
                               final JwtClaims claims,
                               final PublicJsonWebKey jsonWebKey) {
        LOGGER.debug("Service [{}] is set to sign id tokens", svc);
        return EncodingUtils.signJws(claims, jsonWebKey, getJsonWebKeySigningAlgorithm(svc), new HashMap<>(0));
    }

    @Override
    @SneakyThrows
    public JwtClaims decode(final String token, final Optional<OAuthRegisteredService> service) {
        val jsonWebKey = getJsonWebKeySigningKey();
        if (jsonWebKey.getPublicKey() == null) {
            throw new IllegalArgumentException("JSON web key used to validate the id token signature has no associated public key");
        }
        val jwt = EncodingUtils.verifyJwsSignature(jsonWebKey.getPublicKey(), token);
        if (jwt == null) {
            throw new IllegalArgumentException("Unable to verify signature of the token using the JSON web key public key");
        }
        val result = new String(jwt, StandardCharsets.UTF_8);
        val claims = JwtBuilder.parse(result);

        if (StringUtils.isBlank(claims.getIssuer())) {
            throw new IllegalArgumentException("Claims do not container an issuer");
        }

        LOGGER.debug("Validating claims as [{}] with issuer [{}]", claims, claims.getIssuer());
        if (!claims.getIssuer().equalsIgnoreCase(this.issuer)) {
            throw new IllegalArgumentException("Issuer assigned to claims " + claims.getIssuer() + " does not match " + this.issuer);
        }

        if (StringUtils.isBlank(claims.getStringClaim(OAuth20Constants.CLIENT_ID))) {
            throw new IllegalArgumentException("Claims do not contain a client id claim");
        }
        return JwtClaims.parse(claims.toString());
    }

    /**
     * Gets signing key.
     *
     * @return the signing key
     */
    protected abstract PublicJsonWebKey getJsonWebKeySigningKey();

}
