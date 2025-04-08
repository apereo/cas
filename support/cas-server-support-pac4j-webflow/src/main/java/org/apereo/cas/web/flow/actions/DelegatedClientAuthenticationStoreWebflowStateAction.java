package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.util.InitializableObject;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedClientAuthenticationStoreWebflowStateAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientAuthenticationStoreWebflowStateAction extends BaseCasWebflowAction {
    /**
     * Configuration context.
     */
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    /**
     * Webflow state manager.
     */
    protected final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
            .orElseGet(() -> (String) request.getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER));

        val service = WebUtils.getService(requestContext);
        return FunctionUtils.doAndHandle(
                () -> Optional.ofNullable(clientName)
                    .filter(StringUtils::isNotBlank)
                    .map(name -> configContext.getIdentityProviders().findClient(name, webContext))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(client -> isDelegatedClientAuthorizedForService(client, service, requestContext))
                    .stream()
                    .findFirst()
                    .map(IndirectClient.class::cast)
                    .stream()
                    .peek(InitializableObject::init)
                    .findFirst()
                    .map(Unchecked.function(client -> {
                        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, webContext, client);
                        requestContext.getFlowScope().put(TransientSessionTicket.class.getName(), ticket);
                        return ticket;
                    }))
                    .map(ticket -> new EventFactorySupport().event(this,
                        CasWebflowConstants.TRANSITION_ID_REDIRECT, ticket.getClass().getName(), ticket))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> UnauthorizedServiceException.denied("Unable to locate client identity provider %s".formatted(clientName))),
                throwable -> {
                    val message = String.format("Authentication request was denied from the provider %s", clientName);
                    LoggingUtils.warn(LOGGER, message, throwable);
                    throw UnauthorizedServiceException.wrap(throwable);
                })
            .get();
    }

    protected boolean isDelegatedClientAuthorizedForService(final Client client,
                                                            final Service service,
                                                            final RequestContext requestContext) {
        return configContext.getDelegatedClientIdentityProviderAuthorizers()
            .stream()
            .allMatch(Unchecked.predicate(authz -> authz.isDelegatedClientAuthorizedForService(client, service, requestContext)));
    }

}
