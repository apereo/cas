package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DetermineDelegatedAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DetermineDelegatedAuthenticationAction extends AbstractAction implements DisposableBean {
    private final CasConfigurationProperties casProperties;

    private final DelegatedClientIdentityProviderConfigurationProducer providerConfigurationProducer;

    private final transient WatchableGroovyScriptResource watchableScript;

    @Override
    public void destroy() {
        this.watchableScript.close();
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val user = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (user == null) {
            LOGGER.error("Unable to locate passwordless account in the flow");
            return error();
        }

        val clients = providerConfigurationProducer.produce(requestContext);
        if (clients.isEmpty()) {
            LOGGER.debug("No delegated authentication providers are available or defined");
            return success();
        }
        if (!isDelegatedAuthenticationActiveFor(requestContext, user)) {
            LOGGER.debug("User [{}] is not activated to use CAS delegated authentication to external identity providers. "
                + "You may wish to re-examine your CAS configuration to enable and allow for delegated authentication to be "
                + "combined with passwordless authentication", user);
            return success();
        }

        val providerResult = determineDelegatedIdentityProviderConfiguration(requestContext, user, clients);
        if (providerResult.isPresent()) {
            val resolvedId = providerResult.get();
            requestContext.getFlashScope().put("delegatedClientIdentityProvider", resolvedId);
            return new EventFactorySupport().event(this,
                CasWebflowConstants.TRANSITION_ID_REDIRECT, "delegatedClientIdentityProvider", resolvedId);
        }
        LOGGER.trace("Delegated identity provider could not be determined for [{}] based on [{}]", user, clients);
        return success();
    }

    /**
     * Determine delegated identity provider configuration.
     *
     * @param requestContext the request context
     * @param user           the user
     * @param clients        the clients
     * @return the optional
     */
    protected Optional<Serializable> determineDelegatedIdentityProviderConfiguration(final RequestContext requestContext,
                                                                                     final PasswordlessUserAccount user,
                                                                                     final Set<? extends Serializable> clients) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val args = new Object[]{user, clients, request, LOGGER};
        return Optional.ofNullable(watchableScript.execute(args, Serializable.class));
    }

    /**
     * Should delegate authentication for user?
     *
     * @param requestContext the request context
     * @param user           the user
     * @return true/false
     */
    protected boolean isDelegatedAuthenticationActiveFor(final RequestContext requestContext,
                                                         final PasswordlessUserAccount user) {
        val status = user.getDelegatedAuthenticationEligible();
        if (status.isTrue()) {
            LOGGER.trace("Passwordless account [{}] is eligible for delegated authentication", user);
            return true;
        }
        if (status.isFalse()) {
            LOGGER.trace("Passwordless account [{}] is not eligible for delegated authentication", user);
            return false;
        }
        return casProperties.getAuthn().getPasswordless().getCore().isDelegatedAuthenticationActivated();
    }
}
