package org.apereo.cas.oidc.web;

import java.util.Set;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectAction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OidcCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class OidcCasClientRedirectActionBuilder extends OAuth20DefaultCasClientRedirectActionBuilder {
    private final OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport;

    @Override
    public RedirectAction build(final CasClient casClient, final WebContext context) {
        boolean renew = casClient.getConfiguration().isRenew();
        boolean gateway = casClient.getConfiguration().isGateway();

        final Set<String> prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(context);
        if (prompts.contains(OidcConstants.PROMPT_NONE)) {
            renew = false;
            gateway = true;
        } else if (prompts.contains(OidcConstants.PROMPT_LOGIN)
                || oidcAuthorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context)) {
            renew = true;
        }

        final RedirectAction action = super.build(casClient, context, renew, gateway);
        LOGGER.debug("Final redirect action is [{}]", action);
        return action;
    }
}
