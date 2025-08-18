package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.EncodableCipher;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.oauth2.sdk.auth.X509CertificateConfirmation;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OAuth20JwtAccessTokenEncodableCipher}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
class OAuth20JwtAccessTokenEncodableCipher implements EncodableCipher<String, String> {
    private final OAuth20ConfigurationContext configurationContext;
    private final RegisteredService registeredService;
    private final OAuth20Token token;
    private final Service service;
    private final String issuer;
    private final boolean forceEncodeAsJwt;
    private String tokenAudience;

    @Override
    public String encode(final String value, final Object[] parameters) {
        if (registeredService instanceof OAuthRegisteredService && shouldEncodeAsJwt()) {
            return FunctionUtils.doUnchecked(() -> {
                val request = getJwtRequestBuilder();
                return configurationContext.getAccessTokenJwtBuilder().build(request);
            });
        }
        return token.getId();
    }

    protected JwtBuilder.JwtRequest getJwtRequestBuilder() throws Throwable {
        val authentication = token.getAuthentication();
        val builder = JwtBuilder.JwtRequest.builder();
        val attributes = collectAttributes();
        return builder
            .serviceAudience(determineServiceAudience())
            .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
            .jwtId(token.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(determineValidUntilDate())
            .attributes(attributes)
            .registeredService(Optional.of(registeredService))
            .issuer(determineIssuer())
            .service(Optional.ofNullable(service))
            .resolveSubject(token.isStateless())
            .build();
    }

    protected String determineIssuer() {
        return StringUtils.defaultIfBlank(this.issuer, configurationContext.getCasProperties().getServer().getPrefix());
    }

    protected Map<String, List<Object>> collectAttributes() throws Throwable {
        val accessTokenProps = configurationContext.getCasProperties().getAuthn().getOauth().getAccessToken();
        if (token instanceof OAuth20AccessToken && accessTokenProps.isIncludeClaimsInJwt()) {
            return collectClaimsForAccessToken();
        }
        return new HashMap<>();
    }

    protected Map<String, List<Object>> collectClaimsForAccessToken() throws Throwable {
        val activePrincipal = buildPrincipalForAttributeFilter(token, registeredService);
        val principal = configurationContext.getProfileScopeToAttributesFilter()
            .filter(service, activePrincipal, registeredService, (OAuth20AccessToken) token);
        val attributesToRelease = new HashMap<>(principal.getAttributes());
        val originalAttributes = activePrincipal.getAttributes();
        if (originalAttributes.containsKey(OAuth20Constants.DPOP_CONFIRMATION)) {
            CollectionUtils.firstElement(originalAttributes.get(OAuth20Constants.DPOP_CONFIRMATION))
                .ifPresent(conf -> {
                    val confirmation = new JWKThumbprintConfirmation(new Base64URL(conf.toString()));
                    val claim = confirmation.toJWTClaim();
                    attributesToRelease.put(claim.getKey(), List.of(claim.getValue()));
                });
            attributesToRelease.put(OAuth20Constants.DPOP, originalAttributes.get(OAuth20Constants.DPOP));
            attributesToRelease.put(OAuth20Constants.DPOP_CONFIRMATION, originalAttributes.get(OAuth20Constants.DPOP_CONFIRMATION));
        }

        if (originalAttributes.containsKey(OAuth20Constants.X509_CERTIFICATE_DIGEST)) {
            CollectionUtils.firstElement(originalAttributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST))
                .ifPresent(conf -> {
                    val confirmation = new X509CertificateConfirmation(new Base64URL(conf.toString()));
                    val claim = confirmation.toJWTClaim();
                    attributesToRelease.put(claim.getKey(), List.of(claim.getValue()));
                });
            attributesToRelease.put(OAuth20Constants.X509_CERTIFICATE_DIGEST, originalAttributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST));
        }
        FunctionUtils.doIfNotNull(token.getGrantType(), type ->
            attributesToRelease.put(OAuth20Constants.GRANT_TYPE, List.of(type.getType())));
        FunctionUtils.doIfNotNull(token.getResponseType(), type ->
            attributesToRelease.put(OAuth20Constants.RESPONSE_TYPE, List.of(type.getType())));
        attributesToRelease.remove(CasProtocolConstants.PARAMETER_PASSWORD);
        return attributesToRelease;
    }

    protected Date determineValidUntilDate() {
        val authenticationDate = token.getAuthentication().getAuthenticationDate();
        return DateTimeUtils.dateOf(authenticationDate.plusSeconds(token.getExpirationPolicy().getTimeToLive()));
    }

    protected Set<String> determineServiceAudience() {
        if (StringUtils.isNotBlank(tokenAudience)) {
            return Set.of(tokenAudience);
        }
        val oauthRegisteredService = (OAuthRegisteredService) registeredService;
        if (oauthRegisteredService.getAudience().isEmpty()) {
            return Set.of(token.getClientId());
        }
        return oauthRegisteredService.getAudience();
    }

    protected boolean shouldEncodeAsJwt() {
        val oauthRegisteredService = (OAuthRegisteredService) registeredService;
        val oauthProps = configurationContext.getCasProperties().getAuthn().getOauth();

        val dpopRequest = token.getAuthentication().containsAttribute(OAuth20Constants.DPOP);

        val accessTokenAsJwt = token instanceof OAuth20AccessToken
            && (oauthProps.getAccessToken().isCreateAsJwt() || oauthRegisteredService.isJwtAccessToken());
        val refreshTokenAsJwt = token instanceof OAuth20RefreshToken
            && (oauthProps.getRefreshToken().isCreateAsJwt() || oauthRegisteredService.isJwtRefreshToken());

        return this.forceEncodeAsJwt || accessTokenAsJwt || refreshTokenAsJwt || dpopRequest;
    }

    private Principal buildPrincipalForAttributeFilter(final OAuth20Token token,
                                                       final RegisteredService registeredService) throws Throwable {
        val authentication = token.getAuthentication();
        val attributes = new HashMap<>(authentication.getPrincipal().getAttributes());
        val authnAttributes = configurationContext.getAuthenticationAttributeReleasePolicy()
            .getAuthenticationAttributesForRelease(authentication, registeredService);
        attributes.putAll(authnAttributes);
        return configurationContext.getPrincipalFactory().createPrincipal(authentication.getPrincipal().getId(), attributes);
    }
}

