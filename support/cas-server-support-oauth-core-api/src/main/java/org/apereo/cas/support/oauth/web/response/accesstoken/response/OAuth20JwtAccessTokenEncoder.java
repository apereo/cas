package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.DecodableCipher;
import org.apereo.cas.util.crypto.EncodableCipher;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.auth.X509CertificateConfirmation;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OAuth20JwtAccessTokenEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Slf4j
public class OAuth20JwtAccessTokenEncoder {
    /**
     * To decodable cipher.
     *
     * @param accessTokenJwtBuilder the access token jwt builder
     * @param registeredService     the registered service
     * @return the decodable cipher
     */
    public static DecodableCipher<String, String> toDecodableCipher(final JwtBuilder accessTokenJwtBuilder,
                                                                    final RegisteredService registeredService) {
        return new OAuth20JwtAccessTokenDecodableCipher(registeredService, accessTokenJwtBuilder);
    }

    /**
     * To decodable cipher.
     *
     * @param accessTokenJwtBuilder the access token jwt builder
     * @return the decodable cipher
     */
    public static DecodableCipher<String, String> toDecodableCipher(final JwtBuilder accessTokenJwtBuilder) {
        return toDecodableCipher(accessTokenJwtBuilder, null);
    }

    /**
     * To encodable cipher.
     *
     * @param configurationContext the configuration context
     * @param tokenResult          the token result
     * @param accessToken          the access token
     * @param issuer               the issuer
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(
        final OAuth20ConfigurationContext configurationContext,
        final OAuth20AccessTokenResponseResult tokenResult,
        final OAuth20AccessToken accessToken,
        final String issuer) {
        val cipher = new OAuth20JwtAccessTokenEncodableCipher(configurationContext, tokenResult.getRegisteredService(),
            accessToken, tokenResult.getService(), issuer,
            tokenResult.getRequestedTokenType() == OAuth20TokenExchangeTypes.JWT);
        if (tokenResult.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE && tokenResult.getRequestedTokenType() == OAuth20TokenExchangeTypes.JWT) {
            val audience = Optional.ofNullable(tokenResult.getTokenExchangeAudience())
                .or(() -> Optional.ofNullable(tokenResult.getTokenExchangeResource()).map(Service::getId))
                .orElse(StringUtils.EMPTY);
            cipher.setTokenAudience(audience);
        }
        return cipher;
    }

    /**
     * To encodable cipher.
     *
     * @param configurationContext the configuration context
     * @param registeredService    the registered service
     * @param accessToken          the access token
     * @param issuer               the issuer
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(final OAuth20ConfigurationContext configurationContext,
                                                                    final RegisteredService registeredService,
                                                                    final OAuth20AccessToken accessToken,
                                                                    final String issuer) {
        return new OAuth20JwtAccessTokenEncodableCipher(configurationContext, registeredService,
            accessToken, accessToken.getService(), issuer, false);
    }


    /**
     * To encodable cipher.
     *
     * @param configurationContext the configuration context
     * @param registeredService    the registered service
     * @param accessToken          the access token
     * @return the object
     */
    public static EncodableCipher<String, String> toEncodableCipher(final OAuth20ConfigurationContext configurationContext,
                                                                    final OAuthRegisteredService registeredService,
                                                                    final OAuth20AccessToken accessToken) {
        return toEncodableCipher(configurationContext, registeredService,
            accessToken, configurationContext.getCasProperties().getServer().getPrefix());
    }

    /**
     * To encodable cipher.
     *
     * @param configurationContext the configuration context
     * @param registeredService    the registered service
     * @param accessToken          the access token
     * @param service              the service
     * @param forceEncodeAsJwt     the force encode as jwt
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(final OAuth20ConfigurationContext configurationContext,
                                                                    final RegisteredService registeredService,
                                                                    final OAuth20AccessToken accessToken,
                                                                    final Service service,
                                                                    final boolean forceEncodeAsJwt) {
        return new OAuth20JwtAccessTokenEncodableCipher(configurationContext, registeredService,
            accessToken, service, configurationContext.getCasProperties().getServer().getPrefix(), forceEncodeAsJwt);
    }

    /**
     * To encodable cipher.
     *
     * @param configurationContext the configuration context
     * @param tokenResult          the token result
     * @param accessToken          the access token
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(final OAuth20ConfigurationContext configurationContext,
                                                                    final OAuth20AccessTokenResponseResult tokenResult,
                                                                    final OAuth20AccessToken accessToken) {

        val cipher = new OAuth20JwtAccessTokenEncodableCipher(configurationContext, tokenResult.getRegisteredService(),
            accessToken, tokenResult.getService(), configurationContext.getCasProperties().getServer().getPrefix(),
            tokenResult.getRequestedTokenType() == OAuth20TokenExchangeTypes.JWT);
        if (tokenResult.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE && tokenResult.getRequestedTokenType() == OAuth20TokenExchangeTypes.JWT) {
            val audience = Optional.ofNullable(tokenResult.getTokenExchangeAudience())
                .or(() -> Optional.ofNullable(tokenResult.getTokenExchangeResource()).map(Service::getId))
                .orElse(StringUtils.EMPTY);
            cipher.setTokenAudience(audience);
        }
        return cipher;
    }

    @Slf4j
    @RequiredArgsConstructor
    static class OAuth20JwtAccessTokenDecodableCipher implements DecodableCipher<String, String> {
        private final RegisteredService registeredService;
        private final JwtBuilder accessTokenJwtBuilder;

        protected RegisteredService resolveRegisteredService(final Header header) {
            var oAuthRegisteredService = (OAuthRegisteredService) registeredService;
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

        /**
         * Decode a JWT token or return an opaque token as-is.
         * Avoid logging stack trace if JWT parsing fails.
         *
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
    }

    @Slf4j
    @RequiredArgsConstructor
    @Setter
    @Getter
    @Accessors(chain = true)
    static class OAuth20JwtAccessTokenEncodableCipher implements EncodableCipher<String, String> {
        private final OAuth20ConfigurationContext configurationContext;
        private final RegisteredService registeredService;
        private final OAuth20AccessToken accessToken;
        private final Service service;
        private final String issuer;
        private final boolean forceEncodeAsJwt;
        private String tokenAudience;

        @Override
        public String encode(final String value, final Object[] parameters) {
            if (registeredService instanceof final OAuthRegisteredService oAuthRegisteredService
                && shouldEncodeAsJwt(oAuthRegisteredService, accessToken)) {
                return FunctionUtils.doUnchecked(() -> {
                    val request = getJwtRequestBuilder(oAuthRegisteredService, accessToken);
                    return configurationContext.getAccessTokenJwtBuilder().build(request);
                });
            }
            return accessToken.getId();
        }

        protected JwtBuilder.JwtRequest getJwtRequestBuilder(
            final OAuthRegisteredService registeredService,
            final OAuth20AccessToken accessToken) throws Throwable {
            val authentication = accessToken.getAuthentication();
            val builder = JwtBuilder.JwtRequest.builder();
            val attributes = collectAttributes(accessToken, registeredService);
            return builder
                .serviceAudience(determineServiceAudience(registeredService, accessToken))
                .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                .jwtId(accessToken.getId())
                .subject(authentication.getPrincipal().getId())
                .validUntilDate(determineValidUntilDate(accessToken))
                .attributes(attributes)
                .registeredService(Optional.of(registeredService))
                .issuer(StringUtils.defaultIfBlank(this.issuer, configurationContext.getCasProperties().getServer().getPrefix()))
                .service(Optional.ofNullable(service))
                .resolveSubject(accessToken.isStateless())
                .build();
        }

        protected Map<String, List<Object>> collectAttributes(final OAuth20AccessToken accessToken,
                                                              final OAuthRegisteredService registeredService) throws Throwable {

            val activePrincipal = buildPrincipalForAttributeFilter(accessToken, registeredService);
            val principal = configurationContext.getProfileScopeToAttributesFilter()
                .filter(accessToken.getService(), activePrincipal, registeredService, accessToken);

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
            attributesToRelease.remove(CasProtocolConstants.PARAMETER_PASSWORD);
            return attributesToRelease;
        }

        protected Date determineValidUntilDate(final AuthenticationAwareTicket accessToken) {
            val authenticationDate = accessToken.getAuthentication().getAuthenticationDate();
            return DateTimeUtils.dateOf(authenticationDate.plusSeconds(accessToken.getExpirationPolicy().getTimeToLive()));
        }

        protected Set<String> determineServiceAudience(final OAuthRegisteredService registeredService,
                                                       final AuthenticationAwareTicket accessToken) {
            if (StringUtils.isNotBlank(tokenAudience)) {
                return Set.of(tokenAudience);
            }
            if (registeredService.getAudience().isEmpty()) {
                return Set.of(((OAuth20Token) accessToken).getClientId());
            }
            return registeredService.getAudience();
        }

        protected boolean shouldEncodeAsJwt(final OAuthRegisteredService oAuthRegisteredService,
                                            final Ticket accessToken) {
            val serviceRequiresJwt = oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken();
            val dpopRequest = accessToken instanceof final AuthenticationAwareTicket aat
                && aat.getAuthentication().containsAttribute(OAuth20Constants.DPOP);
            return configurationContext.getCasProperties().getAuthn().getOauth().getAccessToken().isCreateAsJwt()
                || this.forceEncodeAsJwt || serviceRequiresJwt || dpopRequest;
        }

        private Principal buildPrincipalForAttributeFilter(final OAuth20AccessToken accessToken,
                                                           final RegisteredService registeredService) throws Throwable {
            val authentication = accessToken.getAuthentication();
            val attributes = new HashMap<>(authentication.getPrincipal().getAttributes());
            val authnAttributes = configurationContext.getAuthenticationAttributeReleasePolicy()
                .getAuthenticationAttributesForRelease(authentication, registeredService);
            attributes.putAll(authnAttributes);
            return configurationContext.getPrincipalFactory().createPrincipal(authentication.getPrincipal().getId(), attributes);
        }
    }


}
