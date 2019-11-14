package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationClientLogoutAction}.
 * <p>
 * The action takes into account the currently used PAC4J client which is stored
 * in the user profile. If the client is found, its logout action is executed.
 * <p>
 * Assumption: The PAC4J user profile should be in the user session during
 * logout, accessible with PAC4J Profile Manager. The Logout web flow should
 * make sure the user profile is present.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedAuthenticationClientLogoutAction extends AbstractAction {
    private final Clients clients;

    private final SessionStore<JEEContext> sessionStore;

    /**
     * Finds the current profile from the context.
     *
     * @param webContext A web context (request + response).
     * @return The common profile active.
     */
    private static CommonProfile findCurrentProfile(final JEEContext webContext) {
        val pm = new ProfileManager<CommonProfile>(webContext, webContext.getSessionStore());
        val profile = pm.get(true);
        return profile.orElse(null);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val context = new JEEContext(request, response, this.sessionStore);

            val currentProfile = findCurrentProfile(context);
            val clientResult = currentProfile == null
                ? Optional.<Client>empty()
                : clients.findClient(currentProfile.getClientName());
            if (clientResult.isPresent()) {
                val client = clientResult.get();
                LOGGER.debug("Located client [{}]", client);
                val actionResult = client.getLogoutAction(context, currentProfile, null);
                if (actionResult.isPresent()) {
                    val action = (HttpAction) actionResult.get();
                    new JEEHttpActionAdapter().adapt(action, context);
                }
            } else {
                LOGGER.debug("The current client cannot be found and no logout action will be executed.");
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

}
