package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.DefaultOAuth20RequestParameterResolver;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcRequestParameterResolver}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
public class OidcRequestParameterResolver extends DefaultOAuth20RequestParameterResolver {
    private final OidcRequestObjectDecrypter requestObjectDecrypter;

    private final OAuth20ClientSecretValidator clientSecretValidator;

    public OidcRequestParameterResolver(final JwtBuilder jwtBuilder, final OidcRequestObjectDecrypter requestObjectDecrypter,
                                        final OAuth20ClientSecretValidator clientSecretValidator) {
        super(jwtBuilder);
        this.requestObjectDecrypter = requestObjectDecrypter;
        this.clientSecretValidator = clientSecretValidator;
    }

    /**
     * A request object signed with HMAC, {@code HS256}, {@code HS384} or {@code HS512}, is verified with the client
     * secrets of the client (OpenID Connect Core 1.0, sections 6.1 and 10.1): its secrets that the client secret validator
     * does not consider expired, decoded with the cipher of the validator, whose
     * octets are at least as long as the hash output of the algorithm (RFC 7518, section 3.2). Request objects signed
     * otherwise are verified with the keys of the client, as the default resolver does. A request object signed with HMAC
     * that names no registered client, or verifies with none of its secrets, is not read, so that the request is refused
     * as an invalid request object instead of failing.
     *
     * @param registeredService the registered service of the client, if known
     * @param requestObject     the request object
     * @return the claims
     */
    @Override
    protected @Nullable JWTClaimsSet unpackRequestObject(@Nullable final RegisteredService registeredService, final String requestObject) {
        val jwt = Unchecked.supplier(() -> JWTParser.parse(requestObject)).get();
        if (!(jwt instanceof final SignedJWT signedJwt) || !JWSAlgorithm.Family.HMAC_SHA.contains(signedJwt.getHeader().getAlgorithm())) {
            return super.unpackRequestObject(registeredService, requestObject);
        }
        if (!(registeredService instanceof final OAuthRegisteredService oauthService)) {
            LOGGER.warn("Request object signed with a client secret names no registered client");
            return null;
        }
        val minimumLength = Unchecked.supplier(() -> MACSigner.getMinRequiredSecretLength(signedJwt.getHeader().getAlgorithm())).get() / Byte.SIZE;
        val verified = oauthService.getClientSecrets()
            .stream()
            .filter(secret -> !clientSecretValidator.isClientSecretExpired(secret, oauthService))
            .map(secret -> SpringExpressionLanguageValueResolver.getInstance().resolve(secret.getValue()))
            .map(secret -> clientSecretValidator.getCipherExecutor().decode(secret, new Object[]{oauthService}))
            .filter(StringUtils::isNotBlank)
            .map(secret -> secret.getBytes(StandardCharsets.UTF_8))
            .filter(secret -> secret.length >= minimumLength)
            .anyMatch(Unchecked.predicate(secret -> signedJwt.verify(new MACVerifier(secret))));
        if (!verified) {
            LOGGER.warn("Request object does not verify with a client secret of [{}]", oauthService.getClientId());
            return null;
        }
        return Unchecked.supplier(signedJwt::getJWTClaimsSet).get();
    }

    /**
     * A request object encrypted to CAS is decrypted first; the nested JWT is then verified like any request object.
     *
     * @param requestObject the request object
     * @return the request object, decrypted
     */
    @Override
    protected String decryptRequestObject(final String requestObject) {
        return OidcRequestObjectDecrypter.isEncrypted(requestObject)
            ? requestObjectDecrypter.decrypt(requestObject)
            : requestObject;
    }

    @Override
    protected List<String> getSupportedScopes(final HttpServletRequest request) {
        return OidcServerDiscoverySettingsFactory.resolveScopesSupported(jwtBuilder.getCasProperties());
    }

    @Override
    protected List<String> getResponseTypesSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
    }

    @Override
    protected List<String> getGrantTypesSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
    }

    @Override
    protected List<String> getResponseModesSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseModesSupported();
    }

    @Override
    protected boolean isRequestParameterSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isRequestParameterSupported();
    }

    @Override
    protected boolean isClaimsParameterSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isClaimsParameterSupported();
    }

    @Override
    protected List<String> getPromptValuesSupported() {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getPromptValuesSupported();
    }
}
