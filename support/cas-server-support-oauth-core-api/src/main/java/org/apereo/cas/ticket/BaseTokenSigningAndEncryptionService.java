package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link BaseTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseTokenSigningAndEncryptionService implements OAuth20TokenSigningAndEncryptionService {
    private final String issuer;

    @SneakyThrows
    protected static String encryptToken(final String encryptionAlg,
                                         final String encryptionEncoding,
                                         final String keyIdHeaderValue,
                                         final Key publicKey,
                                         final String payload) {
        return EncodingUtils.encryptValueAsJwt(publicKey, payload, encryptionAlg,
            encryptionEncoding, keyIdHeaderValue, new HashMap<>(0));
    }

    @Override
    @SneakyThrows
    public JwtClaims decode(final String token, final Optional<OAuthRegisteredService> service) {
        val jsonWebKey = getJsonWebKeySigningKey();
        if (jsonWebKey.getPublicKey() == null) {
            throw new IllegalArgumentException("JSON web key used to validate the id token signature has no associated public key");
        }
        val jwt = Objects.requireNonNull(verifySignature(token, jsonWebKey),
            "Unable to verify signature of the token using the JSON web key public key");
        val result = new String(jwt, StandardCharsets.UTF_8);
        val claims = JwtBuilder.parse(result);

        if (StringUtils.isBlank(claims.getIssuer())) {
            throw new IllegalArgumentException("Claims do not container an issuer");
        }

        validateIssuerClaim(claims);

        if (StringUtils.isBlank(claims.getStringClaim(OAuth20Constants.CLIENT_ID))) {
            throw new IllegalArgumentException("Claims do not contain a client id claim");
        }
        return JwtClaims.parse(claims.toString());
    }

    /**
     * Gets allowed signing algorithms.
     * Returning an empty collection indicates that all algorithms should be supported, except none.
     * @param svc the svc
     * @return the allowed signing algorithms
     */
    public abstract List<String> getAllowedSigningAlgorithms(OAuthRegisteredService svc);

    protected void validateIssuerClaim(final JWTClaimsSet claims) {
        LOGGER.debug("Validating claims as [{}] with issuer [{}]", claims, claims.getIssuer());
        val iss = determineIssuer(claims);
        Objects.requireNonNull(iss, "Issuer cannot be null or undefined");
        if (!claims.getIssuer().equalsIgnoreCase(iss)) {
            throw new IllegalArgumentException("Issuer assigned to claims "
                                               + claims.getIssuer() + " does not match " + iss);
        }
    }

    protected String determineIssuer(final JWTClaimsSet claims) {
        return getIssuer();
    }

    protected String signToken(final OAuthRegisteredService service,
                               final JwtClaims claims,
                               final PublicJsonWebKey jsonWebKey) {
        LOGGER.debug("Service [{}] is set to sign id tokens", service.getServiceId());
        return JsonWebTokenSigner.builder()
            .key(Optional.ofNullable(jsonWebKey)
                .map(PublicJsonWebKey::getPrivateKey)
                .orElse(null))
            .keyId(Optional.ofNullable(jsonWebKey)
                .map(PublicJsonWebKey::getKeyId)
                .orElseGet(() -> UUID.randomUUID().toString()))
            .algorithm(getJsonWebKeySigningAlgorithm(service))
            .allowedAlgorithms(new LinkedHashSet<>(getAllowedSigningAlgorithms(service)))
            .build()
            .sign(claims);
    }

    protected byte[] verifySignature(final String token, final PublicJsonWebKey jsonWebKey) {
        return EncodingUtils.verifyJwsSignature(jsonWebKey.getPublicKey(), token);
    }

    protected abstract PublicJsonWebKey getJsonWebKeySigningKey();
}
