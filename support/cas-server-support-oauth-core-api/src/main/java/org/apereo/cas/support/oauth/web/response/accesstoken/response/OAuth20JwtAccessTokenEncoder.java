package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.auth.X509CertificateConfirmation;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OAuth20JwtAccessTokenEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SuperBuilder
@Getter
@Slf4j
public class OAuth20JwtAccessTokenEncoder implements CipherExecutor<String, String> {
    private final JwtBuilder accessTokenJwtBuilder;

    private final OAuth20AccessToken accessToken;

    private final RegisteredService registeredService;

    private final Service service;

    private final CasConfigurationProperties casProperties;

    private final String issuer;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    /**
     * Decode a JWT token or return an opaque token as-is.
     * Avoid logging stack trace if JWT parsing fails.
     * @param tokenId    encrypted value
     * @param parameters the parameters
     * @return the decoded value.
     * Doing basic checks to reduce logged stack traces when {@link JWTParser#parse} throws {@link ParseException}.
     * Encrypted tokens can have five dot delimited sections and plain or signed tokens have three.
     */
    @Override
    public String decode(final String tokenId, final Object[] parameters) {
        if (StringUtils.isBlank(tokenId)) {
            LOGGER.debug("No access token is provided to decode");
            return tokenId;
        }
        try {
            val header = JWTParser.parse(tokenId).getHeader();
            val claims = accessTokenJwtBuilder.unpack(Optional.ofNullable(resolveRegisteredService(header)), tokenId);
            return claims.getJWTID();
        } catch (final ParseException e) {
            LOGGER.trace("Token is not valid JWT, returning it as-is: [{}]", tokenId);
            return tokenId;
        }
    }

    @Override
    public String encode(final String value, final Object[] parameters) {
        if (registeredService instanceof final OAuthRegisteredService oAuthRegisteredService
            && shouldEncodeAsJwt(oAuthRegisteredService, accessToken)) {
            return FunctionUtils.doUnchecked(() -> {
                val request = getJwtRequestBuilder(oAuthRegisteredService, accessToken);
                return accessTokenJwtBuilder.build(request);
            });
        }
        return accessToken.getId();
    }

    protected JwtBuilder.JwtRequest getJwtRequestBuilder(
        final OAuthRegisteredService registeredService,
        final OAuth20AccessToken accessToken) throws Throwable {
        
        val activePrincipal = buildPrincipalForAttributeFilter(accessToken, registeredService);
        val principal = profileScopeToAttributesFilter.filter(accessToken.getService(), activePrincipal, registeredService, accessToken);
        val attributes = principal.getAttributes();
        
        if (attributes.containsKey(OAuth20Constants.DPOP_CONFIRMATION)) {
            CollectionUtils.firstElement(attributes.get(OAuth20Constants.DPOP_CONFIRMATION))
                .ifPresent(conf -> {
                    val confirmation = new JWKThumbprintConfirmation(new Base64URL(conf.toString()));
                    val claim = confirmation.toJWTClaim();
                    attributes.put(claim.getKey(), List.of(claim.getValue()));
                });
        }

        if (attributes.containsKey(OAuth20Constants.X509_CERTIFICATE_DIGEST)) {
            CollectionUtils.firstElement(attributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST))
                .ifPresent(conf -> {
                    val confirmation = new X509CertificateConfirmation(new Base64URL(conf.toString()));
                    val claim = confirmation.toJWTClaim();
                    attributes.put(claim.getKey(), List.of(claim.getValue()));
                });
        }

        val authentication = accessToken.getAuthentication();
        val builder = JwtBuilder.JwtRequest.builder();
        val validUntilDate = authentication.getAuthenticationDate()
            .plusSeconds(accessToken.getExpirationPolicy().getTimeToLive());

        val serviceAudiences = registeredService.getAudience().isEmpty()
            ? Set.of(accessToken.getClientId())
            : registeredService.getAudience();
        return builder
            .serviceAudience(serviceAudiences)
            .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
            .jwtId(accessToken.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(DateTimeUtils.dateOf(validUntilDate))
            .attributes(attributes)
            .registeredService(Optional.of(registeredService))
            .issuer(StringUtils.defaultIfBlank(this.issuer, casProperties.getServer().getPrefix()))
            .build();
    }

    protected boolean shouldEncodeAsJwt(final OAuthRegisteredService oAuthRegisteredService,
                                        final OAuth20AccessToken accessToken) {
        return casProperties.getAuthn().getOauth().getAccessToken().isCreateAsJwt()
            || (oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken())
            || accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP);
    }

    protected RegisteredService resolveRegisteredService(final Header header) {
        var oAuthRegisteredService = (OAuthRegisteredService) this.registeredService;
        if (oAuthRegisteredService == null) {
            val serviceId = header.getCustomParam(RegisteredServiceCipherExecutor.CUSTOM_HEADER_REGISTERED_SERVICE_ID);
            if (serviceId != null) {
                val serviceIdentifier = Long.parseLong(serviceId.toString());
                oAuthRegisteredService = accessTokenJwtBuilder.getServicesManager()
                    .findServiceBy(serviceIdentifier, OAuthRegisteredService.class);
            }
        }
        return oAuthRegisteredService;
    }

    private Principal buildPrincipalForAttributeFilter(final OAuth20AccessToken accessToken,
                                                       final RegisteredService registeredService) throws Throwable {
        val authentication = accessToken.getAuthentication();
        val attributes = new HashMap<>(authentication.getPrincipal().getAttributes());
        val authnAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(authentication, registeredService);
        attributes.putAll(authnAttributes);
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(authentication.getPrincipal().getId(), attributes);
    }
}
