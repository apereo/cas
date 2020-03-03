package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.profile.ProfileManager;
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

    private final SessionStore<JEEContext> sessionStore;

    public OidcSecurityInterceptor(final Config config, final String name,
                                   final OidcAuthorizationRequestSupport support,
                                   final SessionStore<JEEContext> sessionStore) {
        super(config, name, JEEHttpActionAdapter.INSTANCE);
        this.authorizationRequestSupport = support;
        this.sessionStore = sessionStore;

        setAuthorizers(StringUtils.EMPTY);
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        val ctx = new JEEContext(request, response, this.sessionStore);

        var clearCreds = false;
        val authentication = authorizationRequestSupport.isCasAuthenticationAvailable(ctx);
        if (authentication.isEmpty()) {
            clearCreds = true;
        } else {
            val maxAge = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(ctx);
            if (maxAge.isPresent()) {
                clearCreds = OidcAuthorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(ctx, authentication.get());
            }
        }

        val prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(ctx);

        if (!clearCreds) {
            clearCreds = prompts.contains(OidcConstants.PROMPT_LOGIN);
        }

        if (clearCreds) {
            clearCreds = !prompts.contains(OidcConstants.PROMPT_NONE);
        }

        if (clearCreds) {
            val manager = new ProfileManager<>(ctx, ctx.getSessionStore());
            manager.remove(true);
        }
        return super.preHandle(request, response, handler);
    }
}
