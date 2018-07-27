package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.util.Pac4jUtils;

import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.SecurityInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcSecurityInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcSecurityInterceptor extends SecurityInterceptor {

    private final OidcAuthorizationRequestSupport authorizationRequestSupport;

    public OidcSecurityInterceptor(final Config config, final String name, final OidcAuthorizationRequestSupport support) {
        super(config, name);
        authorizationRequestSupport = support;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws Exception {
        val ctx = Pac4jUtils.getPac4jJ2EContext(request, response);
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);

        var clearCreds = false;
        val authentication = authorizationRequestSupport.isCasAuthenticationAvailable(ctx);
        if (!authentication.isPresent()) {
            clearCreds = true;
        }

        val auth = authorizationRequestSupport.isAuthenticationProfileAvailable(ctx);

        if (auth.isPresent()) {
            val maxAge = authorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(ctx);
            if (maxAge.isPresent()) {
                clearCreds = authorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(ctx, auth.get());
            }
        }

        val prompts = authorizationRequestSupport.getOidcPromptFromAuthorizationRequest(ctx);

        if (!clearCreds) {
            clearCreds = prompts.contains(OidcConstants.PROMPT_LOGIN);
        }

        if (clearCreds) {
            clearCreds = !prompts.contains(OidcConstants.PROMPT_NONE);
        }

        if (clearCreds) {
            manager.remove(true);
        }
        return super.preHandle(request, response, handler);
    }
}
