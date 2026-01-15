package org.apereo.cas.uma.ticket.rpt;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseTokenSigningAndEncryptionService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;

/**
 * This is {@link UmaRequestingPartyTokenSigningService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaRequestingPartyTokenSigningService extends BaseTokenSigningAndEncryptionService {
    private final JsonWebKeySet jsonWebKeySet;

    private final CasConfigurationProperties casProperties;

    public UmaRequestingPartyTokenSigningService(final CasConfigurationProperties properties) {
        val jwksFile = properties.getAuthn().getOauth().getUma().getRequestingPartyToken().getJwksFile().getLocation();
        this.jsonWebKeySet = FunctionUtils.doIf(ResourceUtils.doesResourceExist(jwksFile),
                Unchecked.supplier(() -> {
                    val json = IOUtils.toString(jwksFile.getInputStream(), StandardCharsets.UTF_8);
                    return new JsonWebKeySet(json);
                }),
                () -> {
                    LOGGER.warn("JWKS file for UMA RPT tokens cannot be located. Tokens will not be signed");
                    return new JsonWebKeySet();
                })
            .get();
        this.casProperties = properties;
    }

    @Override
    public String encode(final OAuthRegisteredService registeredService, final JwtClaims claims) throws Throwable {
        LOGGER.debug("Generated claims to put into token are [{}]", claims.toJson());
        return signTokenIfNecessary(claims, registeredService);
    }

    @Override
    public Set<String> getAllowedSigningAlgorithms(final OAuthRegisteredService registeredService) {
        return JsonWebTokenSigner.ALGORITHM_ALL_EXCEPT_NONE;
    }

    @Override
    public PublicJsonWebKey getJsonWebKeySigningKey(final Optional<OAuthRegisteredService> registeredService) throws Throwable {
        val jwks = jsonWebKeySet.getJsonWebKeys();
        FunctionUtils.throwIf(jwks.isEmpty(),
            () -> new IllegalArgumentException("Json web keystore is empty and contains no keys"));
        return (PublicJsonWebKey) jwks.getFirst();
    }

    @Override
    public String resolveIssuer(final Optional<OAuthRegisteredService> registeredService) {
        return casProperties.getAuthn().getOauth().getUma().getCore().getIssuer();
    }
}
