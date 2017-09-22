package org.apereo.cas.support.oauth.web.response;

import org.apereo.cas.CasProtocolConstants;
import org.jasig.cas.client.util.CommonUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link OAuth20DefaultCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20DefaultCasClientRedirectActionBuilder implements OAuth20CasClientRedirectActionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20DefaultCasClientRedirectActionBuilder.class);

    @Override
    public RedirectAction build(final CasClient casClient, final WebContext context) {
        try {
            final CasConfiguration casConfiguration = casClient.getConfiguration();
            final String redirectionUrl = CommonUtils.constructRedirectUrl(casConfiguration.getLoginUrl(),
                    CasProtocolConstants.PARAMETER_SERVICE,
                    casClient.computeFinalCallbackUrl(context),
                    casConfiguration.isRenew(), casConfiguration.isGateway());
            LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
            return RedirectAction.redirect(redirectionUrl);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
