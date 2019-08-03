package org.apereo.cas.support.oauth.web.response;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.util.EncodingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.RedirectionAction;

import java.util.Optional;

/**
 * This is {@link OAuth20DefaultCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OAuth20DefaultCasClientRedirectActionBuilder implements OAuth20CasClientRedirectActionBuilder {

    @Override
    public Optional<RedirectionAction> build(final CasClient casClient, final WebContext context) {
        val casConfiguration = casClient.getConfiguration();
        return build(casClient, context, casConfiguration.isRenew(), casConfiguration.isGateway());
    }

    /**
     * Build with predefined renew and gateway parameters.
     *
     * @param casClient the cas client config
     * @param context   the context
     * @param renew     ask for credentials again
     * @param gateway   skip asking for credentials
     * @return the redirect action
     */
    protected Optional<RedirectionAction> build(final CasClient casClient, final WebContext context, final boolean renew, final boolean gateway) {
        val serviceUrl = casClient.computeFinalCallbackUrl(context);
        val casServerLoginUrl = casClient.getConfiguration().getLoginUrl();
        val redirectionUrl = casServerLoginUrl + (casServerLoginUrl.contains("?") ? "&" : "?")
            + CasProtocolConstants.PARAMETER_SERVICE + '=' + EncodingUtils.urlEncode(serviceUrl)
            + (renew ? '&' + CasProtocolConstants.PARAMETER_RENEW + "=true" : StringUtils.EMPTY)
            + (gateway ? '&' + CasProtocolConstants.PARAMETER_GATEWAY + "=true" : StringUtils.EMPTY);
        LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
        return Optional.of(new FoundAction(redirectionUrl));
    }
}
