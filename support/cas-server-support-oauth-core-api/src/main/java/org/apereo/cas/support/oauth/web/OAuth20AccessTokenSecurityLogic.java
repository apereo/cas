package org.apereo.cas.support.oauth.web;

import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.http.adapter.HttpActionAdapter;

/**
 * This is {@link OAuth20AccessTokenSecurityLogic}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class OAuth20AccessTokenSecurityLogic extends DefaultSecurityLogic {
    @Override
    public Object perform(final WebContext context,
                          final SessionStore sessionStore,
                          final Config config,
                          final SecurityGrantedAccessAdapter securityGrantedAccessAdapter,
                          final HttpActionAdapter httpActionAdapter,
                          final String clients, final String authorizers,
                          final String matchers, final Object... parameters) {
        val manager = this.getProfileManager(context, sessionStore);
        manager.setConfig(config);
        manager.removeProfiles();
        return super.perform(context, sessionStore, config,
            securityGrantedAccessAdapter, httpActionAdapter, clients,
            authorizers, matchers, parameters);
    }
}
