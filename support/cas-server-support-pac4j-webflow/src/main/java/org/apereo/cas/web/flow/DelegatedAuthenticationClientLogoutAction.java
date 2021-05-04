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
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.saml.state.SAML2StateGenerator;
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

    private final SessionStore sessionStore;

    @Override
    protected Event doPreExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);

        val currentProfile = findCurrentProfile(context);
        val clientResult = currentProfile == null
            ? Optional.<Client>empty()
            : clients.findClient(currentProfile.getClientName());
        if (clientResult.isPresent()) {
            val client = clientResult.get();
            LOGGER.debug("Handling logout for delegated authentication client [{}]", client);
            WebUtils.putDelegatedAuthenticationClientName(requestContext, client.getName());
            sessionStore.set(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, client.getName());
        }
        return null;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);

        val currentProfile = findCurrentProfile(context);
        val clientResult = currentProfile == null
            ? Optional.<Client>empty()
            : clients.findClient(currentProfile.getClientName());
        if (clientResult.isPresent()) {
            val client = clientResult.get();
            LOGGER.trace("Located client [{}]", client);

            val service = WebUtils.getService(requestContext);
            val targetUrl = service != null ? service.getId() : null;
            LOGGER.debug("Logout target url based on service [{}] is [{}]", service, targetUrl);

            val actionResult = client.getLogoutAction(context, sessionStore, currentProfile, targetUrl);
            if (actionResult.isPresent()) {
                val action = (HttpAction) actionResult.get();
                LOGGER.debug("Adapting logout action [{}] for client [{}]", action, client);
                JEEHttpActionAdapter.INSTANCE.adapt(action, context);
            }
        } else {
            LOGGER.debug("The current client cannot be found; No logout action can execute");
        }
        return null;
    }

    /**
     * Finds the current profile from the context.
     *
     * @param webContext A web context (request + response).
     * @return The common profile active.
     */
    private UserProfile findCurrentProfile(final JEEContext webContext) {
        val pm = new ProfileManager(webContext, this.sessionStore);
        val profile = pm.getProfile();
        return profile.orElse(null);
    }
}
