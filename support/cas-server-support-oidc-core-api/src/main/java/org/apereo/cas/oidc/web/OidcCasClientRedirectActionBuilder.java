package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;

import java.util.Optional;

/**
 * This is {@link OidcCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcCasClientRedirectActionBuilder extends OAuth20DefaultCasClientRedirectActionBuilder {
    private final OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport;

    @Override
    public Optional<RedirectionAction> build(final CasClient casClient, final WebContext context) {
        var renew = casClient.getConfiguration().isRenew();
        var gateway = casClient.getConfiguration().isGateway();

        val prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(context);
        if (prompts.contains(OidcConstants.PROMPT_NONE)) {
            renew = false;
            gateway = true;
        } else if (prompts.contains(OidcConstants.PROMPT_LOGIN)
            || oidcAuthorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context)) {
            renew = true;
        }

        val action = super.build(casClient, context, renew, gateway);
        LOGGER.debug("Final redirect action is [{}]", action);
        return action;
    }
}
