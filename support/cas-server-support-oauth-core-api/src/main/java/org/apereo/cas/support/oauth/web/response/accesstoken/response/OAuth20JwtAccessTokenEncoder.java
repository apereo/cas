package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.DecodableCipher;
import org.apereo.cas.util.crypto.EncodableCipher;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

/**
 * This is {@link OAuth20JwtAccessTokenEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Slf4j
@UtilityClass
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
     * @param token          the access token
     * @param issuer               the issuer
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(
        final OAuth20ConfigurationContext configurationContext,
        final OAuth20AccessTokenResponseResult tokenResult,
        final OAuth20Token token,
        final String issuer) {
        val cipher = new OAuth20JwtAccessTokenEncodableCipher(configurationContext, tokenResult.getRegisteredService(),
            token, tokenResult.getService(), issuer,
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
     * @param token          the access token
     * @param service              the service
     * @param forceEncodeAsJwt     the force encode as jwt
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(final OAuth20ConfigurationContext configurationContext,
                                                                    final RegisteredService registeredService,
                                                                    final OAuth20Token token,
                                                                    final Service service,
                                                                    final boolean forceEncodeAsJwt) {
        return new OAuth20JwtAccessTokenEncodableCipher(configurationContext, registeredService,
            token, service, configurationContext.getCasProperties().getServer().getPrefix(), forceEncodeAsJwt);
    }

    /**
     * To encodable cipher.
     *
     * @param configurationContext the configuration context
     * @param tokenResult          the token result
     * @param token          the access token
     * @return the encodable cipher
     */
    public static EncodableCipher<String, String> toEncodableCipher(final OAuth20ConfigurationContext configurationContext,
                                                                    final OAuth20AccessTokenResponseResult tokenResult,
                                                                    final OAuth20Token token) {
        val cipher = new OAuth20JwtAccessTokenEncodableCipher(configurationContext, tokenResult.getRegisteredService(),
            token, tokenResult.getService(), configurationContext.getCasProperties().getServer().getPrefix(),
            tokenResult.getRequestedTokenType() == OAuth20TokenExchangeTypes.JWT);
        if (tokenResult.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE && tokenResult.getRequestedTokenType() == OAuth20TokenExchangeTypes.JWT) {
            val audience = Optional.ofNullable(tokenResult.getTokenExchangeAudience())
                .or(() -> Optional.ofNullable(tokenResult.getTokenExchangeResource()).map(Service::getId))
                .orElse(StringUtils.EMPTY);
            cipher.setTokenAudience(audience);
        }
        return cipher;
    }
}
