package org.apereo.cas.web;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServicePropertyGroups;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link BaseDelegatedAuthenticationController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseDelegatedAuthenticationController {

    /**
     * Endpoint path controlled by this controller that receives the response to PAC4J.
     */
    protected static final String ENDPOINT_RESPONSE = "login/{clientName}";

    private final DelegatedClientAuthenticationConfigurationContext configurationContext;

    /**
     * Gets redirection action.
     *
     * @param client     the client
     * @param webContext the web context
     * @param ticket     the ticket
     * @return the redirection action
     */
    protected Optional<RedirectionAction> getRedirectionAction(final IndirectClient client, final WebContext webContext,
                                                               final TransientSessionTicket ticket) {
        val properties = ticket.getProperties();
        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
        }
        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true);
        }

        if (ticket.getService() != null) {
            configureWebContextForRegisteredService(webContext, ticket);
        }

        configurationContext.getDelegatedClientAuthenticationRequestCustomizers()
            .stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .filter(c -> c.supports(client, webContext))
            .forEach(c -> c.customize(client, webContext));

        return client.getRedirectionActionBuilder()
            .getRedirectionAction(webContext, configurationContext.getSessionStore());
    }

    /**
     * Configure web context for service overrides.
     *
     * @param registeredService the registered service
     * @param webContext        the web context
     * @param properties        the properties
     */
    protected void configureWebContextForRegisteredServiceProperties(final RegisteredService registeredService,
                                                                     final WebContext webContext,
                                                                     final List<RegisteredServiceProperties> properties) {
        properties.stream()
            .filter(prop -> prop.isAssignedTo(registeredService))
            .forEach(prop -> webContext.setRequestAttribute(prop.getPropertyName(), prop.getTypedPropertyValue(registeredService)));
    }

    /**
     * Build redirect view back to flow view.
     *
     * @param clientName the client name
     * @param request    the request
     * @return the view
     */
    @SneakyThrows
    protected View buildRedirectViewBackToFlow(final String clientName, final HttpServletRequest request) {
        val urlBuilder = new URIBuilder(configurationContext.getCasProperties().getServer().getLoginUrl());
        request.getParameterMap().forEach((k, v) -> {
            val value = request.getParameter(k);
            urlBuilder.addParameter(k, value);
        });
        urlBuilder.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, clientName);
        val url = urlBuilder.toString();
        LOGGER.debug("Received response from client [{}]; Redirecting to [{}]", clientName, url);
        return new RedirectView(url);
    }

    /**
     * Gets resulting view.
     *
     * @param client     the client
     * @param webContext the web context
     * @param ticket     the ticket
     * @return the resulting view
     */
    @SneakyThrows
    protected View getResultingView(final IndirectClient client, final WebContext webContext,
                                    final TransientSessionTicket ticket) {
        client.init();
        val actionResult = getRedirectionAction(client, webContext, ticket);
        if (actionResult.isPresent()) {
            val action = actionResult.get();
            LOGGER.debug("Determined final redirect action for client [{}] as [{}]", client, action);
            if (action instanceof WithLocationAction) {
                val foundAction = WithLocationAction.class.cast(action);
                val builder = new URIBuilder(foundAction.getLocation());
                val url = builder.toString();
                LOGGER.debug("Redirecting client [{}] to [{}] based on identifier [{}]", client.getName(), url, ticket.getId());
                return new RedirectView(url);
            }
            if (action instanceof WithContentAction) {
                val seeOtherAction = WithContentAction.class.cast(action);
                return new DynamicHtmlView(seeOtherAction.getContent());
            }
        }
        LOGGER.warn("Unable to determine redirect action for client [{}]", client);
        return null;
    }

    /**
     * Configure web context for registered service.
     *
     * @param webContext the web context
     * @param ticket     the ticket
     */
    protected void configureWebContextForRegisteredService(final WebContext webContext, final TransientSessionTicket ticket) {
        val registeredService = configurationContext.getServicesManager().findServiceBy(ticket.getService());
        val audit = AuditableContext.builder()
            .service(ticket.getService())
            .registeredService(registeredService)
            .build();
        val result = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
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
}
