package org.apereo.cas.support.oauth.web.response;

import org.apereo.cas.CasProtocolConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.util.CommonUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectAction;

/**
 * This is {@link OAuth20DefaultCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OAuth20DefaultCasClientRedirectActionBuilder implements OAuth20CasClientRedirectActionBuilder {


    @Override
    public RedirectAction build(final CasClient casClient, final WebContext context) {
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
    protected RedirectAction build(final CasClient casClient, final WebContext context, final boolean renew, final boolean gateway) {
        val redirectionUrl = CommonUtils.constructRedirectUrl(casClient.getConfiguration().getLoginUrl(),
            CasProtocolConstants.PARAMETER_SERVICE,
            casClient.computeFinalCallbackUrl(context),
            renew, gateway);
        LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
        return RedirectAction.redirect(redirectionUrl);
    }
}
