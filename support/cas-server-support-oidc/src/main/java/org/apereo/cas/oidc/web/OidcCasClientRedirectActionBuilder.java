package org.apereo.cas.oidc.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Optional;

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
        final Optional<Authentication> auth = oidcAuthorizationRequestSupport.isCasAuthenticationAvailable(context);
        auth.ifPresent(authentication -> oidcAuthorizationRequestSupport.configureClientForMaxAgeAuthorizationRequest(casClient, context, authentication));

        OidcAuthorizationRequestSupport.configureClientForPromptLoginAuthorizationRequest(casClient, context);
        OidcAuthorizationRequestSupport.configureClientForPromptNoneAuthorizationRequest(casClient, context);

        final RedirectAction action = super.build(casClient, context);
        LOGGER.debug("Final redirect action is [{}]", action);
        return action;
    }
}
