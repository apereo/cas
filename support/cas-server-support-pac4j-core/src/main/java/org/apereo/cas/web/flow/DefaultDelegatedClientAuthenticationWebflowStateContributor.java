package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DefaultDelegatedClientAuthenticationWebflowStateContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefaultDelegatedClientAuthenticationWebflowStateContributor implements DelegatedClientAuthenticationWebflowStateContributor {
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    /**
     * Build the ticket properties.
     *
     * @param webContext the web context
     * @return the ticket properties
     */
    protected Map<String, Serializable> buildTicketProperties(final WebContext webContext) {
        val properties = new HashMap<String, Serializable>();
        val themeParamName = configContext.getCasProperties().getTheme().getParamName();
        val localParamName = configContext.getCasProperties().getLocale().getParamName();
        webContext.getRequestParameter(themeParamName).ifPresent(value -> properties.put(themeParamName, value));
        webContext.getRequestParameter(localParamName).ifPresent(value -> properties.put(localParamName, value));
        webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)
            .ifPresent(value -> properties.put(CasProtocolConstants.PARAMETER_METHOD, value));
        LOGGER.debug("Built ticket properties [{}]", properties);
        return properties;
    }

    @Override
    public Map<String, ? extends Serializable> store(final RequestContext requestContext, final WebContext webContext,
                                                     final Client client) throws Throwable {
        val httpRequest = ((JEEContext) webContext).getNativeRequest();
        val originalService = configContext.getArgumentExtractor().extractService(httpRequest);
        val service = configContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(originalService);

        val properties = buildTicketProperties(webContext);
        properties.put(CasProtocolConstants.PARAMETER_SERVICE, originalService);
        properties.put(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service);
        properties.put(Client.class.getName(), client.getName());

        val registeredService = configContext.getServicesManager().findServiceBy(service);
        webContext.getRequestParameter(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)
            .or(() -> Optional.of(Boolean.toString(RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.isAssignedTo(registeredService))))
            .filter(value -> Strings.CI.equals(value, "true"))
            .ifPresent(attr -> properties.put(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true));
        webContext.getRequestParameter(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)
            .or(() -> Optional.of(Boolean.toString(RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_PASSIVE_AUTHN.isAssignedTo(registeredService))))
            .filter(value -> Strings.CI.equals(value, "true"))
            .ifPresent(attr -> properties.put(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true));
        if (registeredService != null && !registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)) {
            properties.put(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
        }
        return properties;
    }

    @Override
    public Service restore(final RequestContext requestContext,
                           final WebContext webContext,
                           final Optional<TransientSessionTicket> givenSessionTicket,
                           final Client client) {
        return givenSessionTicket
            .map(ticket -> {
                val service = ticket.getService();
                LOGGER.trace("Restoring requested service [{}] back in the authentication flow", service);
                WebUtils.putServiceIntoFlowScope(requestContext, service);
                webContext.setRequestAttribute(CasWebflowConstants.ATTRIBUTE_SERVICE, service);

                val themeParamName = configContext.getCasProperties().getTheme().getParamName();
                val localParamName = configContext.getCasProperties().getLocale().getParamName();

                val properties = ticket.getProperties();
                webContext.setRequestAttribute(themeParamName, properties.get(themeParamName));

                val localeValue = properties.get(localParamName);
                Optional.ofNullable(localeValue)
                    .ifPresent(locale -> {
                        webContext.setRequestAttribute(localParamName, locale);
                        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                        Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
                            .ifPresent(localeResolver -> localeResolver.setLocale(request, response, Locale.forLanguageTag(locale.toString())));
                    });
                webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_METHOD, properties.get(CasProtocolConstants.PARAMETER_METHOD));
                return service;
            })
            .orElse(null);
    }
}
