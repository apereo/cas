package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasDelegatingWebflowEventResolver},
 * which handles the initial authentication attempt and calls upon a number of
 * embedded resolvers to produce the next event in the authentication flow.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class DefaultCasDelegatingWebflowEventResolver extends AbstractCasWebflowEventResolver
    implements CasDelegatingWebflowEventResolver {

    private final List<CasWebflowEventResolver> orderedResolvers = new ArrayList<>();

    private final CasWebflowEventResolver selectiveResolver;

    public DefaultCasDelegatingWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext configurationContext,
                                                    final CasWebflowEventResolver selectiveResolver) {
        super(configurationContext);
        this.selectiveResolver = selectiveResolver;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) throws Throwable {
        val credentials = getCredentialFromContext(context);

        val service = locateServiceForRequest(context);
        LOGGER.trace("Resolved service [{}]", service);

        try {
            if (credentials != null && !credentials.isEmpty()) {
                val agent = WebUtils.getHttpServletRequestUserAgentFromRequestContext(context);
                val geoLocation = WebUtils.getHttpServletRequestGeoLocationFromRequestContext(context);
                val properties = CollectionUtils.<String, Serializable>wrap(
                    CredentialMetadata.PROPERTY_USER_AGENT, agent,
                    CredentialMetadata.PROPERTY_GEO_LOCATION, geoLocation);
                credentials.forEach(cred -> {
                    cred.getCredentialMetadata().putProperties(properties);
                    getConfigurationContext().getTenantExtractor().extract(context).ifPresent(tenant ->
                        cred.getCredentialMetadata().setTenant(tenant.getId()));
                });
                val builder = getConfigurationContext().getAuthenticationSystemSupport()
                    .handleInitialAuthenticationTransaction(service, credentials.toArray(Credential.EMPTY_CREDENTIALS_ARRAY));
                builder.collect(credentials.toArray(Credential.EMPTY_CREDENTIALS_ARRAY));

                builder.getInitialAuthentication().ifPresent(authn -> {
                    WebUtils.putAuthenticationResultBuilder(builder, context);
                    WebUtils.putAuthentication(authn, context);
                });
            }

            val registeredService = determineRegisteredServiceForEvent(context, service);
            LOGGER.trace("Attempting to resolve candidate authentication events for service [{}]", service);
            val resolvedEvents = resolveCandidateAuthenticationEvents(context, service, registeredService);
            if (resolvedEvents.isEmpty()) {
                LOGGER.trace("No candidate authentication events were resolved for service [{}]", service);
            } else {
                LOGGER.trace("Authentication events resolved for [{}] are [{}]. Selecting final event...", service, resolvedEvents);
                WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
                val finalResolvedEvent = this.selectiveResolver.resolveSingle(context);
                LOGGER.debug("The final authentication event resolved for [{}] is [{}]", service, finalResolvedEvent);
                if (finalResolvedEvent != null) {
                    return CollectionUtils.wrapSet(finalResolvedEvent);
                }
            }

            val builder = WebUtils.getAuthenticationResultBuilder(context);
            if (builder == null) {
                val msg = "Unable to locate authentication object in the webflow context";
                throw new IllegalArgumentException(new AuthenticationException(msg));
            }
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Throwable exception) {
            val event = buildEventFromException(exception, context, credentials, service);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            LOGGER.debug("Authentication request failed with [{}], resulting in event [{}]", response.getStatus(), event);
            return CollectionUtils.wrapSet(event);
        }
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver resolver) {
        if (BeanSupplier.isNotProxy(resolver)) {
            orderedResolvers.add(resolver);
        }
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver resolver, final int index) {
        if (BeanSupplier.isNotProxy(resolver)) {
            orderedResolvers.add(index, resolver);
        }
    }

    protected Collection<Event> resolveCandidateAuthenticationEvents(final RequestContext context,
                                                                     final Service service,
                                                                     final RegisteredService registeredService) {
        return orderedResolvers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(Unchecked.function(resolver -> {
                LOGGER.debug("Resolving candidate authentication event for service [{}] using [{}]", service, resolver.getName());
                return resolver.resolveSingle(context);
            }))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Event::getId))
            .collect(Collectors.toList());
    }

    protected Event buildEventFromException(final Throwable exception,
                                            final RequestContext requestContext,
                                            final List<Credential> credential,
                                            final Service service) {
        val event = WebflowExceptionTranslator.from(exception, requestContext);
        LoggingUtils.warn(LOGGER, exception);
        val attributes = new LocalAttributeMap<>();
        attributes.put(CasWebflowConstants.TRANSITION_ID_ERROR, event.getSource());
        if (!credential.isEmpty()) {
            attributes.put(Credential.class.getName(), credential.getFirst());
        }
        attributes.put(WebApplicationService.class.getName(), service);
        attributes.putAll(event.getAttributes());
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        attributes.put("url", HttpRequestUtils.getFullRequestUrl(request));
        return newEvent(event.getId(), attributes);
    }

    private RegisteredService determineRegisteredServiceForEvent(final RequestContext context, final Service service) throws Throwable {
        if (service == null) {
            return null;
        }
        LOGGER.trace("Locating authentication event in the request context...");
        val authn = WebUtils.getAuthentication(context);
        if (authn == null) {
            val msg = "Unable to locate authentication object in the webflow context";
            throw new IllegalArgumentException(new AuthenticationException(msg));
        }
        LOGGER.trace("Locating service [{}] in service registry to determine authentication policy", service);
        val registeredService = getConfigurationContext().getServicesManager().findServiceBy(service);
        LOGGER.trace("Enforcing access strategy policies for registered service [{}] and principal [{}]",
            registeredService, authn.getPrincipal());
        val unauthorizedRedirectUrl = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl();
        if (unauthorizedRedirectUrl != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, unauthorizedRedirectUrl);
        }

        val attributeReleaseContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(authn.getPrincipal())
            .applicationContext(getConfigurationContext().getApplicationContext())
            .build();
        val releasingAttributes = registeredService.getAttributeReleasePolicy().getAttributes(attributeReleaseContext);
        releasingAttributes.putAll(authn.getAttributes());

        val accessStrategyAttributes = CoreAuthenticationUtils.mergeAttributes(
            authn.getPrincipal().getAttributes(), releasingAttributes);
        val accessStrategyPrincipal = getConfigurationContext().getPrincipalFactory()
            .createPrincipal(authn.getPrincipal().getId(), accessStrategyAttributes);

        val audit = AuditableContext.builder()
            .service(service)
            .principal(accessStrategyPrincipal)
            .registeredService(registeredService)
            .build();
        val result = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        result.throwExceptionIfNeeded();
        return registeredService;
    }

    protected Service locateServiceForRequest(final RequestContext context) throws Throwable {
        val strategies = getConfigurationContext().getAuthenticationRequestServiceSelectionStrategies();
        val serviceFromFlow = WebUtils.getService(context);
        val serviceFromRequest = WebUtils.getService(getConfigurationContext().getArgumentExtractors(), context);
        if (serviceFromFlow == null) {
            return strategies.resolveService(serviceFromRequest);
        }
        if (serviceFromRequest != null) {
            val fragment = serviceFromRequest.getFragment();
            if (fragment != null) {
                serviceFromFlow.setFragment(fragment);
            }
        }
        return strategies.resolveService(serviceFromFlow);
    }
}
