package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This is {@link BaseTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@NoArgsConstructor(force = true)
@Getter
public abstract class BaseTokenSigningAndEncryptionService implements OAuth20TokenSigningAndEncryptionService {
    @Override
    public JwtClaims decode(final String token, final Optional<OAuthRegisteredService> service) {
        return FunctionUtils.doUnchecked(() -> {
            val jsonWebKey = getJsonWebKeySigningKey();
            FunctionUtils.throwIf(jsonWebKey.getPublicKey() == null,
                () -> new IllegalArgumentException("JSON web key to validate the id token signature has no public key"));
            val jwt = Objects.requireNonNull(verifySignature(token, jsonWebKey),
                "Unable to verify signature of the token using the JSON web key public key");
            val result = new String(jwt, StandardCharsets.UTF_8);
            val claims = JwtBuilder.parse(result);

            FunctionUtils.throwIf(StringUtils.isBlank(claims.getIssuer()),
                () -> new IllegalArgumentException("Claims do not contain an issuer"));

            validateIssuerClaim(claims, service);

            FunctionUtils.throwIf(StringUtils.isBlank(claims.getStringClaim(OAuth20Constants.CLIENT_ID)),
                () -> new IllegalArgumentException("Claims do not contain a client id claim"));
            return JwtClaims.parse(claims.toString());
        });
    }

    /**
     * Gets allowed signing algorithms.
     * Returning an empty collection indicates that all algorithms should be supported, except none.
     *
     * @param svc the svc
     * @return the allowed signing algorithms
     */
    public abstract Set<String> getAllowedSigningAlgorithms(OAuthRegisteredService svc);

    protected void validateIssuerClaim(final JWTClaimsSet claims, final Optional<OAuthRegisteredService> service) {
        LOGGER.debug("Validating claims as [{}] with issuer [{}]", claims, claims.getIssuer());
        val iss = resolveIssuer(service);
        Objects.requireNonNull(iss, "Issuer cannot be null or undefined");
        FunctionUtils.throwIf(!claims.getIssuer().equalsIgnoreCase(iss),
            () -> new IllegalArgumentException("Issuer assigned to claims "
                                               + claims.getIssuer() + " does not match " + iss));
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
