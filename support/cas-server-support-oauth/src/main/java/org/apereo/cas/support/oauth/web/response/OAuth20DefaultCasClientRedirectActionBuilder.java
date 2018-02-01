package org.apereo.cas.support.oauth.web.response;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.jasig.cas.client.util.CommonUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
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
    @SneakyThrows
    public RedirectAction build(final CasClient casClient, final WebContext context) {
        final CasConfiguration casConfiguration = casClient.getConfiguration();
        final String redirectionUrl = CommonUtils.constructRedirectUrl(casConfiguration.getLoginUrl(),
            CasProtocolConstants.PARAMETER_SERVICE,
            casClient.computeFinalCallbackUrl(context),
            casConfiguration.isRenew(), casConfiguration.isGateway());
        LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
        return RedirectAction.redirect(redirectionUrl);
    }
}
