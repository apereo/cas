package org.apereo.cas.web;

import org.apereo.cas.token.TokenConstants;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultTokenRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultTokenRequestExtractor implements TokenRequestExtractor {
    @Override
    public String extract(final HttpServletRequest request) {
        var authTokenValue = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(authTokenValue)) {
            authTokenValue = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);
        }
        return authTokenValue;
    }
}
