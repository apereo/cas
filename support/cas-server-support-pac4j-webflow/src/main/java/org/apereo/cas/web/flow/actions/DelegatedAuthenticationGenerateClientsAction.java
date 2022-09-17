package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.flow.DelegatedAuthenticationSingleSignOnEvaluator;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link DelegatedAuthenticationGenerateClientsAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DelegatedAuthenticationGenerateClientsAction extends BaseCasWebflowAction {
    private final DelegatedAuthenticationSingleSignOnEvaluator singleSignOnEvaluator;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        produceDelegatedAuthenticationClientsForContext(requestContext);
        return success();
    }

    /**
     * Produce delegated authentication clients for context.
     *
     * @param context the context
     */
    protected void produceDelegatedAuthenticationClientsForContext(final RequestContext context) {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val providers = singleSignOnEvaluator.configurationContext()
            .getDelegatedClientIdentityProvidersProducer().produce(context);
        LOGGER.trace("Delegated authentication providers are finalized as [{}]", providers);
        WebUtils.createCredential(context);
        if (HttpStatus.resolve(response.getStatus()).is2xxSuccessful()) {
            singleSignOnEvaluator.configurationContext()
                .getDelegatedClientIdentityProviderConfigurationPostProcessor()
                .process(context, providers);
            if (!singleSignOnEvaluator.singleSignOnSessionExists(context)) {
                handleServerAutoRedirectIfAny(context, providers);
            }
        }
    }

    protected void handleServerAutoRedirectIfAny(final RequestContext context,
                                                 final Set<DelegatedClientIdentityProviderConfiguration> providers) {

        providers
            .stream()
            .filter(provider -> provider.getAutoRedirectType() == DelegationAutoRedirectTypes.SERVER)
            .findFirst()
            .ifPresent(Unchecked.consumer(provider -> {
                LOGGER.debug("Redirecting to [{}]", provider.getRedirectUrl());
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                response.sendRedirect(provider.getRedirectUrl());
            }));
    }

}
