package org.apereo.cas.support.oauth.web.response;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.util.EncodingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20DefaultCasClientRedirectActionBuilder}, which sends the request to the CAS login with neither
 * {@code renew} nor {@code gateway}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultCasClientRedirectActionBuilder implements OAuth20CasClientRedirectActionBuilder {
    private final String casServerLoginUrl;

    @Override
    public String build(final HttpServletRequest request, final String serviceUrl) {
        return build(serviceUrl, false, false);
    }

    /**
     * Build the login URL with the given {@code renew} and {@code gateway} parameters.
     *
     * @param serviceUrl the callback URL that CAS is to send the service ticket to
     * @param renew      ask for credentials again
     * @param gateway    skip asking for credentials
     * @return the login URL
     */
    protected String build(final String serviceUrl, final boolean renew, final boolean gateway) {
        val redirectionUrl = casServerLoginUrl + (casServerLoginUrl.contains("?") ? "&" : "?")
            + CasProtocolConstants.PARAMETER_SERVICE + '=' + EncodingUtils.urlEncode(serviceUrl)
            + (renew ? '&' + CasProtocolConstants.PARAMETER_RENEW + "=true" : StringUtils.EMPTY)
            + (gateway ? '&' + CasProtocolConstants.PARAMETER_GATEWAY + "=true" : StringUtils.EMPTY);
        LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
        return redirectionUrl;
    }
}
