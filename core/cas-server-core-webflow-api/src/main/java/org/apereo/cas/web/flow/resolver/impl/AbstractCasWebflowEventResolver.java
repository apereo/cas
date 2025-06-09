package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowCredentialProvider;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * This is {@link AbstractCasWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle core webflow changes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractCasWebflowEventResolver implements CasWebflowEventResolver {

    protected final EventFactorySupport eventFactory = new EventFactorySupport();
    
    private final CasWebflowEventResolutionConfigurationContext configurationContext;

    @Override
    public Set<Event> resolve(final RequestContext context) throws Throwable {
        LOGGER.trace("Attempting to resolve authentication event using resolver [{}]", getName());
        WebUtils.putWarnCookieIfRequestParameterPresent(configurationContext.getWarnCookieGenerator(), context);
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        return resolveInternal(context);
    }

    @Override
    public Event resolveSingle(final RequestContext context) throws Throwable {
        val events = resolve(context);
        if (events == null || events.isEmpty()) {
            LOGGER.trace("No event could be determined");
            return null;
        }
        val event = events.iterator().next();
        LOGGER.debug("Resolved single event [{}] via [{}] for this context",
            event.getId(), event.getSource().getClass().getName());
        val targetState = WebUtils.getTargetTransition(context);
        return FunctionUtils.doIf(StringUtils.isNotBlank(targetState) && event.getId().equals(CasWebflowConstants.TRANSITION_ID_SUCCESS),
                () -> eventFactory.event(this, targetState),
                () -> event)
            .get();
    }

    /**
     * New event based on the id, which contains an error attribute referring to the exception occurred.
     *
     * @param id    the id
     * @param error the error
     * @return the event
     */
    protected Event newEvent(final String id, final Throwable error) {
        return newEvent(id, new LocalAttributeMap<Serializable>(CasWebflowConstants.TRANSITION_ID_ERROR, error));
    }

    /**
     * New event.
     *
     * @param id the id
     * @return the event
     */
    protected Event newEvent(final String id) {
        return newEvent(id, new LocalAttributeMap<>());
    }

    protected Event newEvent(final String id, final AttributeMap attributes) {
        return new Event(this, id, attributes);
    }

    protected List<Credential> getCredentialFromContext(final RequestContext context) {
        val applicationContext = context.getActiveFlow().getApplicationContext();
        val credentialProvider = ApplicationContextProvider.getBean(applicationContext, CasWebflowCredentialProvider.BEAN_NAME, CasWebflowCredentialProvider.class)
            .orElseGet(() -> new DefaultCasWebflowCredentialProvider(configurationContext.getTenantExtractor()));
        return credentialProvider.extract(context);
    }

    protected Event grantTicketGrantingTicketToAuthenticationResult(final RequestContext context,
                                                                    final AuthenticationResultBuilder authenticationResultBuilder,
                                                                    final Service service) {
        WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, context);
        WebUtils.putServiceIntoFlowScope(context, service);
        return newEvent(CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }

    protected Service resolveServiceFromAuthenticationRequest(final Service service) throws Throwable {
        return configurationContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(service);
    }

    protected Service resolveServiceFromAuthenticationRequest(final RequestContext context) throws Throwable {
        val ctxService = WebUtils.getService(context);
        return resolveServiceFromAuthenticationRequest(ctxService);
    }

    protected Set<Event> handleAuthenticationTransactionAndGrantTicketGrantingTicket(final RequestContext context) throws Throwable {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        try {
            val credentials = getCredentialFromContext(context);
            val builderResult = WebUtils.getAuthenticationResultBuilder(context);

            LOGGER.debug("Handling authentication transaction for credentials [{}]", credentials);
            val service = WebUtils.getService(context);
            val builder = configurationContext.getAuthenticationSystemSupport()
                .handleAuthenticationTransaction(service, builderResult, credentials.toArray(Credential.EMPTY_CREDENTIALS_ARRAY));

            LOGGER.debug("Issuing ticket-granting tickets for service [{}]", service);
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            configurationContext.getCasWebflowExceptionCatalog().translateException(context, e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(getAuthenticationFailureErrorEvent(context, e));
        }
    }

    protected Event getAuthenticationFailureErrorEvent(final RequestContext context,
                                                       final Exception exception) {
        return eventFactory.error(this, exception);
    }

}
