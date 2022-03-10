package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseTokenSigningAndEncryptionService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This is {@link UmaRequestingPartyTokenSigningService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
public class UmaRequestingPartyTokenSigningService extends BaseTokenSigningAndEncryptionService {
    private final PublicJsonWebKey jsonWebKeySigningKey;

    private final CasConfigurationProperties casProperties;

    public UmaRequestingPartyTokenSigningService(final CasConfigurationProperties properties) {
        super(properties.getAuthn().getOauth().getUma().getCore().getIssuer());
        val jwksFile = properties.getAuthn().getOauth().getUma().getRequestingPartyToken().getJwksFile().getLocation();
        jsonWebKeySigningKey = FunctionUtils.doIf(ResourceUtils.doesResourceExist(jwksFile),
                Unchecked.supplier(() -> {
                    val json = IOUtils.toString(jwksFile.getInputStream(), StandardCharsets.UTF_8);
                    val jsonWebKeySet = new JsonWebKeySet(json);
                    val keys = jsonWebKeySet.getJsonWebKeys();
                    return RsaJsonWebKey.class.cast(keys.get(0));
                }),
                () -> {
                    LOGGER.warn("JWKS file for UMA RPT tokens cannot be located. Tokens will not be signed");
                    return null;
                })
            .get();
        this.casProperties = properties;
    }

    @Override
    public String encode(final OAuthRegisteredService service, final JwtClaims claims) {
        LOGGER.debug("Generated claims to put into token are [{}]", claims.toJson());
        return signToken(service, claims, jsonWebKeySigningKey);
    }

    @Override
    public List<String> getAllowedSigningAlgorithms(final OAuthRegisteredService svc) {
        return JsonWebTokenSigner.ALGORITHM_ALL_EXCEPT_NONE;
    }
}
