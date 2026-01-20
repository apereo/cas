package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.DecodableCipher;
import com.nimbusds.jose.Header;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OAuth20JwtAccessTokenDecodableCipher}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */

@Slf4j
@RequiredArgsConstructor
class OAuth20JwtAccessTokenDecodableCipher implements DecodableCipher<String, String> {
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
            return claims == null ? null : claims.getJWTID();
        } catch (final ParseException e) {
            LOGGER.trace("Token is not valid JWT, returning it as-is: [{}]", tokenId);
            return tokenId;
        }
    }
}
