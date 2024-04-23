package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.pac4j.saml.state.SAML2StateGenerator;
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
public class DelegatedAuthenticationClientLogoutAction extends BaseCasWebflowAction {
    protected final DelegatedIdentityProviders identityProviders;

    protected final SessionStore sessionStore;

    @Override
    protected Event doPreExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);

        val currentProfile = findCurrentProfile(context);
        val clientResult = findCurrentClient(currentProfile);
        if (clientResult.isPresent()) {
            val client = clientResult.get();
            LOGGER.debug("Handling logout for delegated authentication client [{}]", client);
            DelegationWebflowUtils.putDelegatedAuthenticationClientName(requestContext, client.getName());
            sessionStore.set(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, client.getName());
        }
        return null;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);

        val currentProfile = findCurrentProfile(context);
        val clientResult = findCurrentClient(currentProfile);
        if (clientResult.isPresent()) {
            val client = clientResult.get();
            LOGGER.trace("Located client [{}]", client);
            val service = WebUtils.getService(requestContext);
            val targetUrl = Optional.ofNullable(service).map(Principal::getId).orElse(null);
            LOGGER.debug("Logout target url based on service [{}] is [{}]", service, targetUrl);

            val callContext = new CallContext(context, sessionStore);
            val actionResult = client.getLogoutAction(callContext, currentProfile, targetUrl);
            if (actionResult.isPresent()) {
                val action = (HttpAction) actionResult.get();
                val logoutAction = DelegatedAuthenticationClientLogoutRequest.builder()
                    .status(action.getCode())
                    .message(action.getMessage())
                    .build();
                DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(requestContext, logoutAction);
                LOGGER.debug("Adapting logout action [{}] for client [{}]", action, client);
                JEEHttpActionAdapter.INSTANCE.adapt(action, context);
            }
        } else {
            LOGGER.debug("The current client cannot be found; No logout action can execute");
        }
        return null;
    }

    protected UserProfile findCurrentProfile(final JEEContext webContext) {
        val pm = new ProfileManager(webContext, this.sessionStore);
        val profile = pm.getProfile();
        return profile.orElse(null);
    }

    protected Optional<Client> findCurrentClient(final UserProfile currentProfile) {
        return currentProfile == null
                ? Optional.empty()
                : identityProviders.findClient(currentProfile.getClientName());
    }
}
