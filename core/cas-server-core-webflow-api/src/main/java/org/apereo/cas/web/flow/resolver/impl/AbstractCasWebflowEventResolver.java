package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link AbstractCasWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle core webflow changes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class AbstractCasWebflowEventResolver implements CasWebflowEventResolver {

    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";

    private final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext;

    /**
     * New event based on the id, which contains an error attribute referring to the exception occurred.
     *
     * @param id    the id
     * @param error the error
     * @return the event
     */
    protected Event newEvent(final String id, final Throwable error) {
        return newEvent(id, new LocalAttributeMap(CasWebflowConstants.TRANSITION_ID_ERROR, error));
    }

    /**
     * New event event.
     *
     * @param id the id
     * @return the event
     */
    protected Event newEvent(final String id) {
        return newEvent(id, new LocalAttributeMap<>());
    }

    /**
     * New event based on the given id.
     *
     * @param id         the id
     * @param attributes the attributes
     * @return the event
     */
    protected Event newEvent(final String id, final AttributeMap attributes) {
        return new Event(this, id, attributes);
    }

    /**
     * Gets credential from context.
     *
     * @param context the context
     * @return the credential from context
     */
    protected Credential getCredentialFromContext(final RequestContext context) {
        return WebUtils.getCredential(context);
    }

    /**
     * Grant ticket granting ticket.
     *
     * @param context                     the context
     * @param authenticationResultBuilder the authentication result builder
     * @param service                     the service
     * @return the event
     */
    protected Event grantTicketGrantingTicketToAuthenticationResult(final RequestContext context,
                                                                    final AuthenticationResultBuilder authenticationResultBuilder,
                                                                    final Service service) {
        WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, context);
        WebUtils.putServiceIntoFlowScope(context, service);
        return newEvent(CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }


    @Override
    public Set<Event> resolve(final RequestContext context) {
        LOGGER.trace("Attempting to resolve authentication event using resolver [{}]", getName());
        WebUtils.putWarnCookieIfRequestParameterPresent(webflowEventResolutionConfigurationContext.getWarnCookieGenerator(), context);
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        return resolveInternal(context);
    }

    @Override
    public Event resolveSingle(final RequestContext context) {
        val events = resolve(context);
        if (events == null || events.isEmpty()) {
            LOGGER.trace("No event could be determined");
            return null;
        }
        val event = events.iterator().next();
        LOGGER.debug("Resolved single event [{}] via [{}] for this context", event.getId(), event.getSource().getClass().getName());
        return event;
    }



    /**
     * Resolve service from authentication request.
     *
     * @param service the service
     * @return the service
     */
    protected Service resolveServiceFromAuthenticationRequest(final Service service) {
        return webflowEventResolutionConfigurationContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(service);
    }

    /**
     * Resolve service from authentication request service.
     *
     * @param context the context
     * @return the service
     */
    protected Service resolveServiceFromAuthenticationRequest(final RequestContext context) {
        val ctxService = WebUtils.getService(context);
        return resolveServiceFromAuthenticationRequest(ctxService);
    }

    /**
     * Handle authentication transaction and grant ticket granting ticket.
     *
     * @param context the context
     * @return the set
     */
    protected Set<Event> handleAuthenticationTransactionAndGrantTicketGrantingTicket(final RequestContext context) {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        try {
            val credential = getCredentialFromContext(context);
            val builderResult = WebUtils.getAuthenticationResultBuilder(context);

            LOGGER.debug("Handling authentication transaction for credential [{}]", credential);
            val service = WebUtils.getService(context);
            val builder = webflowEventResolutionConfigurationContext.getAuthenticationSystemSupport()
                .handleAuthenticationTransaction(service, builderResult, credential);

            LOGGER.debug("Issuing ticket-granting tickets for service [{}]", service);
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            val messageContext = context.getMessageContext();
            messageContext.addMessage(new MessageBuilder()
                .error()
                .code(DEFAULT_MESSAGE_BUNDLE_PREFIX.concat(e.getClass().getSimpleName()))
                .build());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(getAuthenticationFailureErrorEvent(context));
        }
    }

    /**
     * Gets authentication failure error event.
     *
     * @param context the context
     * @return the authentication failure error event
     */
    protected Event getAuthenticationFailureErrorEvent(final RequestContext context) {
        return new EventFactorySupport().error(this);
    }

}
