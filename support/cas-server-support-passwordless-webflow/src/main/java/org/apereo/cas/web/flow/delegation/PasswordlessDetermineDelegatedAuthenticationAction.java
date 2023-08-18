package org.apereo.cas.web.flow.delegation;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.flow.BasePasswordlessCasWebflowAction;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link PasswordlessDetermineDelegatedAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class PasswordlessDetermineDelegatedAuthenticationAction extends BasePasswordlessCasWebflowAction implements DisposableBean {
    private final DelegatedClientIdentityProviderConfigurationProducer providerConfigurationProducer;

    private final WatchableGroovyScriptResource watchableScript;

    public PasswordlessDetermineDelegatedAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final DelegatedClientIdentityProviderConfigurationProducer providerConfigurationProducer,
        final WatchableGroovyScriptResource watchableScript) {
        super(casProperties);
        this.providerConfigurationProducer = providerConfigurationProducer;
        this.watchableScript = watchableScript;
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (user == null) {
            LOGGER.error("Unable to locate passwordless account in the flow");
            return error();
        }

        val clients = FunctionUtils.doUnchecked(() -> providerConfigurationProducer.produce(requestContext));
        if (clients.isEmpty()) {
            LOGGER.debug("No delegated authentication providers are available or defined");
            return success();
        }
        if (!isDelegatedAuthenticationActiveFor(requestContext, user)) {
            LOGGER.debug("User [{}] is not activated to use CAS delegated authentication to external identity providers. "
                         + "You may wish to re-examine your CAS configuration to enable and allow for delegated authentication to be "
                         + "combined with passwordless authentication", user);
            DelegationWebflowUtils.putDelegatedAuthenticationDisabled(requestContext, true);
            return success();
        }
        DelegationWebflowUtils.putDelegatedAuthenticationDisabled(requestContext, false);
        return FunctionUtils.doUnchecked(() -> {
            val providerResult = determineDelegatedIdentityProviderConfiguration(requestContext, user, clients);
            if (providerResult.isPresent()) {
                val clientConfig = providerResult.get();
                if (clientConfig instanceof final DelegatedClientIdentityProviderConfiguration client) {
                    DelegationWebflowUtils.putDelegatedAuthenticationProviderPrimary(requestContext, client);
                    val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                    request.setAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, client.getName());
                    return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROMPT);
                }
            }
            LOGGER.trace("Delegated identity provider could not be determined for [{}] based on [{}]", user, clients);
            return success();
        });
    }

    protected Optional<Serializable> determineDelegatedIdentityProviderConfiguration(final RequestContext requestContext,
                                                                                     final PasswordlessUserAccount user,
                                                                                     final Set<? extends Serializable> clients) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val args = new Object[]{user, clients, request, LOGGER};
        return Optional.ofNullable(watchableScript.execute(args, Serializable.class));
    }

}
