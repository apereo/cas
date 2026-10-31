package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.web.OAuth20CasAuthenticationInterceptor;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcCasAuthenticationInterceptor}, which also refuses the profile in the session for an authorization
 * request with {@code prompt=login}, or whose {@code max_age} the authentication behind the profile exceeds
 * (OpenID Connect Core, section 3.1.2.1), so the user signs in again.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcCasAuthenticationInterceptor extends OAuth20CasAuthenticationInterceptor {
    public OidcCasAuthenticationInterceptor(final CasSessionStore sessionStore,
                                            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                            final TicketRegistry ticketRegistry,
                                            final ServicesManager servicesManager,
                                            final OAuth20RequestParameterResolver requestParameterResolver,
                                            final OAuth20CasCallbackUrlResolver callbackUrlResolver,
                                            final OAuth20CasClientRedirectActionBuilder casClientRedirectActionBuilder) {
        super(sessionStore, ticketGrantingTicketCookieGenerator, ticketRegistry, servicesManager,
            requestParameterResolver, callbackUrlResolver, casClientRedirectActionBuilder);
    }

    @Override
    protected boolean isAuthenticationUsable(final HttpServletRequest request, final OAuth20UserProfile profile) {
        val prompts = requestParameterResolver.resolveSupportedPromptValues(request);
        LOGGER.debug("Located OpenID Connect prompts from request as [{}]", prompts);
        if (prompts.contains(OidcConstants.PROMPT_LOGIN) || OidcRequestSupport.isZeroMaxAgeAuthorizationRequest(request)
            || OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, profile)) {
            LOGGER.debug("Authorization request asks for a fresh login; the profile in the session is not used");
            return false;
        }
        return super.isAuthenticationUsable(request, profile);
    }
}
