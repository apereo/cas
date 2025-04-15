package org.apereo.cas.web.flow.actions;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.principal.provision.DelegatedAuthenticationFailureException;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServicePropertyGroups;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DelegatedClientAuthenticationRedirectAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientAuthenticationRedirectAction extends BaseCasWebflowAction {
    /**
     * Configuration context.
     */
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    /**
     * Webflow state manager.
     */
    protected final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val ticket = requestContext.getFlowScope().get(TransientSessionTicket.class.getName(), TransientSessionTicket.class);
        val client = locateClientIdentityProvider(ticket, requestContext);
        initializeClientIdentityProvider(client);
        val action = getRedirectionAction(ticket, requestContext);
        LOGGER.debug("Determined final redirect action for client [{}] as [{}]", client, action.toString());
        if (action instanceof WithLocationAction) {
            LOGGER.debug("Redirecting client [{}] based on identifier [{}]", client.getName(), ticket.getId());
            handleIdentityProviderWithExternalRedirect(requestContext, client, action);
        }
        if (action instanceof WithContentAction) {
            handleIdentityProviderWithDynamicContent(requestContext, client, action);
        }
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }

    protected RedirectionAction getRedirectionAction(final TransientSessionTicket ticket,
                                                     final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        val properties = ticket.getProperties();
        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
        }

        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true);
        }

        Optional.ofNullable(ticket.getService())
            .ifPresent(Unchecked.consumer(service -> configureWebContextForRegisteredService(webContext, ticket)));

        val clientName = ticket.getProperty(Client.class.getName(), String.class);
        return configContext.getIdentityProviders().findClient(clientName, webContext)
            .map(IndirectClient.class::cast)
            .stream()
            .peek(client -> configContext.getDelegatedClientAuthenticationRequestCustomizers()
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .filter(Unchecked.predicate(contributor -> contributor.supports(client, webContext)))
                .forEach(Unchecked.consumer(contributor -> contributor.customize(client, webContext))))
            .map(client -> {
                val callContext = new CallContext(webContext, configContext.getSessionStore());
                return client.getRedirectionActionBuilder().getRedirectionAction(callContext);
            })
            .flatMap(Optional::stream)
            .findFirst()
            .orElseThrow();
    }

    protected void configureWebContextForRegisteredService(final WebContext webContext,
                                                           final TransientSessionTicket ticket) throws Throwable {
        val registeredService = configContext.getServicesManager().findServiceBy(ticket.getService());
        val audit = AuditableContext.builder()
            .service(ticket.getService())
            .registeredService(registeredService)
            .build();
        val result = configContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        result.throwExceptionIfNeeded();

        if (!registeredService.getProperties().isEmpty()) {
            val delegatedAuthnProperties = Arrays.stream(RegisteredServiceProperties.values())
                .filter(prop -> prop.isMemberOf(RegisteredServicePropertyGroups.DELEGATED_AUTHN))
                .collect(Collectors.toList());
            configureWebContextForRegisteredServiceProperties(registeredService, webContext, delegatedAuthnProperties);

            val saml2ServiceProperties = Arrays.stream(RegisteredServiceProperties.values())
                .filter(prop -> prop.isMemberOf(RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2))
                .collect(Collectors.toList());
            configureWebContextForRegisteredServiceProperties(registeredService, webContext, saml2ServiceProperties);

            val oidcProperties = Arrays.stream(RegisteredServiceProperties.values())
                .filter(prop -> prop.isMemberOf(RegisteredServicePropertyGroups.DELEGATED_AUTHN_OIDC))
                .collect(Collectors.toList());
            configureWebContextForRegisteredServiceProperties(registeredService, webContext, oidcProperties);
        }
    }

    protected void initializeClientIdentityProvider(final IndirectClient client) throws Throwable {
        client.init();
        FunctionUtils.throwIf(!client.isInitialized(), DelegatedAuthenticationFailureException::new);
    }

    protected IndirectClient locateClientIdentityProvider(final TransientSessionTicket ticket, final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        
        val clientName = ticket.getProperty(Client.class.getName(), String.class);
        return configContext.getIdentityProviders().findClient(clientName, webContext)
            .map(IndirectClient.class::cast)
            .stream()
            .findFirst()
            .orElseThrow();
    }

    protected void handleIdentityProviderWithDynamicContent(final RequestContext requestContext,
                                                            final IndirectClient client,
                                                            final RedirectionAction action) throws Exception {
        val seeOtherAction = (WithContentAction) action;
        val view = new DynamicHtmlView(seeOtherAction.getContent());
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        LOGGER.debug("Rendering dynamic content [{}] for client [{}]", view.html(), client.getName());
        view.render(Map.of(), request, response);
        requestContext.getExternalContext().recordResponseComplete();
    }

    protected void handleIdentityProviderWithExternalRedirect(final RequestContext requestContext,
                                                              final IndirectClient client,
                                                              final RedirectionAction action) throws Exception {
        val foundAction = (WithLocationAction) action;
        val builder = new URIBuilder(foundAction.getLocation());
        val url = builder.toString();
        LOGGER.debug("Redirecting to [{}] via client [{}]", url, client.getName());
        requestContext.getExternalContext().requestExternalRedirect(url);
    }

    protected void configureWebContextForRegisteredServiceProperties(
        final RegisteredService registeredService,
        final WebContext webContext,
        final List<RegisteredServiceProperties> properties) {
        properties.stream()
            .filter(prop -> prop.isAssignedTo(registeredService))
            .forEach(prop -> webContext.setRequestAttribute(prop.getPropertyName(), prop.getTypedPropertyValue(registeredService)));
    }

}
