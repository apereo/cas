package org.apereo.cas.web.pac4j;

import java.util.List;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.springframework.web.SecurityInterceptor;

/**
 * This is {@link CasSecurityInterceptor}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
public class CasSecurityInterceptor extends SecurityInterceptor {

    public CasSecurityInterceptor(final Config config, final String clients,
            final String authorizers) {

        super(config, clients, authorizers);

        final DefaultSecurityLogic secLogic = new DefaultSecurityLogic() {
            @Override
            protected HttpAction unauthorized(final WebContext context, final List currentClients) {
                return HttpAction.forbidden("Access Denied", context);
            }

            @Override
            protected boolean loadProfilesFromSession(final WebContext context, final List currentClients) {
                return true;
            }
        };
        secLogic.setSaveProfileInSession(true);
        setSecurityLogic(secLogic);
    }
}
