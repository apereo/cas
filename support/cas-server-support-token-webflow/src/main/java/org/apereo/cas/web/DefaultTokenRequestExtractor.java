package org.apereo.cas.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.token.TokenConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultTokenRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultTokenRequestExtractor implements TokenRequestExtractor {
    @Override
    public String extract(final HttpServletRequest request) {
        String authTokenValue = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(authTokenValue)) {
            authTokenValue = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);
        }
        return authTokenValue;
    }
}
