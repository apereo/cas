package org.apereo.cas.web;

import org.apereo.cas.token.TokenConstants;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link TokenRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface TokenRequestExtractor {

    /**
     * Default extractor token request.
     *
     * @return the token request extractor
     */
    static TokenRequestExtractor defaultExtractor() {
        return new TokenRequestExtractor() {
        };
    }

    /**
     * Extract string.
     *
     * @param request the request
     * @return the string
     */
    default String extract(final HttpServletRequest request) {
        var authTokenValue = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(authTokenValue)) {
            authTokenValue = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);
        }
        return authTokenValue;
    }
}
