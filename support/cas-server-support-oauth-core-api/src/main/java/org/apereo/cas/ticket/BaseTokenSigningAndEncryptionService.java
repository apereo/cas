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
import java.util.Map;
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
    public JwtClaims decode(final String token, final Optional<OAuthRegisteredService> registeredService) {
        return FunctionUtils.doUnchecked(() -> {
            val jsonWebKey = getJsonWebKeySigningKey(registeredService);
            FunctionUtils.throwIf(jsonWebKey.getPublicKey() == null,
                () -> new IllegalArgumentException("JSON web key to validate the ID token signature has no public key"));
            val jwt = Objects.requireNonNull(verifySignature(token, jsonWebKey),
                "Unable to verify signature of the token using the JSON web key public key");
            val result = new String(jwt, StandardCharsets.UTF_8);
            val claims = JwtBuilder.parse(result);

            FunctionUtils.throwIf(StringUtils.isBlank(claims.getIssuer()),
                () -> new IllegalArgumentException("Claims do not contain an issuer"));

            validateIssuerClaim(claims, registeredService);

            FunctionUtils.throwIf(StringUtils.isBlank(claims.getStringClaim(OAuth20Constants.CLIENT_ID)),
                () -> new IllegalArgumentException("Claims do not contain a client id claim"));
            return JwtClaims.parse(claims.toString());
        });
    }

    /**
     * Gets allowed signing algorithms.
     * Returning an empty collection indicates that all algorithms should be supported, except none.
     *
     * @param registeredService the svc
     * @return the allowed signing algorithms
     */
    public abstract Set<String> getAllowedSigningAlgorithms(OAuthRegisteredService registeredService);

    protected void validateIssuerClaim(final JWTClaimsSet claims, final Optional<OAuthRegisteredService> service) throws Throwable {
        LOGGER.debug("Validating claims as [{}] with issuer [{}]", claims, claims.getIssuer());
        val iss = resolveIssuer(service);
        Objects.requireNonNull(iss, "Issuer cannot be null or undefined");
        FunctionUtils.throwIf(!claims.getIssuer().equalsIgnoreCase(iss),
            () -> new IllegalArgumentException("Issuer assigned to JWT claim " + claims.getIssuer() + " does not match " + iss));
    }

    protected String signToken(final OAuthRegisteredService registeredService,
                               final JwtClaims claims,
                               final PublicJsonWebKey jsonWebKey) {
        LOGGER.debug("Service [{}] is set to sign id tokens", registeredService.getServiceId());
        return JsonWebTokenSigner.builder()
            .key(Optional.ofNullable(jsonWebKey)
                .map(PublicJsonWebKey::getPrivateKey)
                .orElse(null))
            .keyId(Optional.ofNullable(jsonWebKey)
                .map(PublicJsonWebKey::getKeyId)
                .orElseGet(() -> UUID.randomUUID().toString()))
            .algorithm(getJsonWebKeySigningAlgorithm(registeredService, jsonWebKey))
            .allowedAlgorithms(new LinkedHashSet<>(getAllowedSigningAlgorithms(registeredService)))
            .mediaType(getSigningMediaType())
            .headers(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()))
            .build()
            .sign(claims);
    }

    protected String getSigningMediaType() {
        return "JWT";
    }

    protected String signTokenIfNecessary(final JwtClaims claims, final OAuthRegisteredService registeredService) throws Throwable {
        if (shouldSignToken(registeredService)) {
            LOGGER.debug("Fetching JSON web key to sign the token for : [{}]", registeredService.getClientId());
            val jsonWebKey = getJsonWebKeySigningKey(Optional.of(registeredService));
            LOGGER.debug("Found JSON web key to sign the token: [{}]", jsonWebKey);
            Objects.requireNonNull(jsonWebKey.getPrivateKey(), "JSON web key used to sign the token has no associated private key");
            return signToken(registeredService, claims, jsonWebKey);
        }
        val claimSet = JwtBuilder.parse(claims.toJson());
        return JwtBuilder.buildPlain(claimSet, Optional.of(registeredService));
    }

    protected byte[] verifySignature(final String token, final PublicJsonWebKey jsonWebKey) {
        return EncodingUtils.verifyJwsSignature(jsonWebKey.getPublicKey(), token);
    }
}
