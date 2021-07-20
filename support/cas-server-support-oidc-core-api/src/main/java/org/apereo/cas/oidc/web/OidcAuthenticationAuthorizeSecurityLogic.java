package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcAuthenticationAuthorizeSecurityLogic}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class OidcAuthenticationAuthorizeSecurityLogic extends DefaultSecurityLogic {
    @Override
    protected List<UserProfile> loadProfiles(final ProfileManager manager, final WebContext context,
                                             final SessionStore sessionStore, final List<Client> clients) {
        val prompts = OidcRequestSupport.getOidcPromptFromAuthorizationRequest(context);
        LOGGER.debug("Located OpenID Connect prompts from request as [{}]", prompts);
        return prompts.contains(OidcConstants.PROMPT_LOGIN)
            ? new ArrayList<>(0)
            : super.loadProfiles(manager, context, sessionStore, clients);
    }
}
