package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.logout.LogoutConfirmationResolver;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link DelegatedSaml2ClientTerminateSessionAction}.
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
public class DelegatedSaml2ClientTerminateSessionAction extends BaseCasWebflowAction {
    private final DelegatedIdentityProviders identityProviders;
    private final SessionStore sessionStore;
    private final LogoutConfirmationResolver logoutConfirmationResolver;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        if (logoutConfirmationResolver.isLogoutRequestConfirmed(requestContext)) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val context = new JEEContext(request, response);

            val currentProfile = findCurrentProfile(context);
            val clientResult = findCurrentClient(currentProfile, context);
            if (clientResult.isPresent()) {
                val client = clientResult.get();
                DelegationWebflowUtils.putDelegatedAuthenticationClientName(requestContext, client.getName());
                LOGGER.debug("Starting logout SAML2 relay state attribute for delegated authentication client [{}]", client);
                sessionStore.set(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, client.getName());
            }
        }
        return null;
    }

    protected Optional<? extends Client> findCurrentClient(final UserProfile currentProfile, final WebContext webContext) {
        return currentProfile == null
            ? Optional.empty()
            : identityProviders.findClient(currentProfile.getClientName(), webContext);
    }

    protected UserProfile findCurrentProfile(final WebContext webContext) {
        val pm = new ProfileManager(webContext, this.sessionStore);
        val profile = pm.getProfile();
        return profile.orElse(null);
    }
}
