package org.apereo.cas.config;

import org.apereo.cas.CasProtocolConstants;
import org.jasig.cas.client.util.CommonUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultOAuthCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("defaultOAuthCasClientRedirectActionBuilder")
public class DefaultOAuthCasClientRedirectActionBuilder implements OAuthCasClientRedirectActionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOAuthCasClientRedirectActionBuilder.class);
    
    @Override
    public RedirectAction build(final CasClient casClient, final WebContext context) {
        try {
            final String redirectionUrl = CommonUtils.constructRedirectUrl(casClient.getCasLoginUrl(),
                    CasProtocolConstants.PARAMETER_SERVICE,
                    casClient.computeFinalCallbackUrl(context), casClient.isRenew(), casClient.isGateway());
            return RedirectAction.redirect(redirectionUrl);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
