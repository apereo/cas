package org.apereo.cas.config;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This is {@link OidcCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("oidcCasClientRedirectActionBuilder")
public class OidcCasClientRedirectActionBuilder extends DefaultOAuthCasClientRedirectActionBuilder {

    @Autowired
    @Qualifier("oidcAuthorizationRequestSupport")
    private OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport;

    @Override
    public RedirectAction build(final CasClient casClient, final WebContext context) {
        final Optional<Authentication> auth = oidcAuthorizationRequestSupport.isCasAuthenticationAvailable(context);
        if (auth.isPresent()) {
            oidcAuthorizationRequestSupport.configureClientForMaxAgeAuthorizationRequest(casClient, context, auth.get());
        }
        return super.build(casClient, context);
    }
}
