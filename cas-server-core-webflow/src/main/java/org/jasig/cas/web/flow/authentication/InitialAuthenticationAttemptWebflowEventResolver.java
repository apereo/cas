package org.jasig.cas.web.flow.authentication;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UnrecognizedAuthenticationMethodException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategySupport;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitialAuthenticationAttemptWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("initialAuthenticationAttemptWebflowEventResolver")
public class InitialAuthenticationAttemptWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    public Event resolveInternal(final RequestContext context) {
        try {

            final Credential credential = getCredentialFromContext(context);
            final AuthenticationResultBuilder authenticationResultBuilder =
                    this.authenticationSystemSupport.handleInitialAuthenticationTransaction(credential);

            final Service service = WebUtils.getService(context);
            if (service != null) {
                final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
                RegisteredServiceAccessStrategySupport.ensureServiceAccessIsAllowed(service, registeredService);

                if (StringUtils.isNotBlank(registeredService.getAuthenticationPolicy().getAuthenticationMethod())) {
                    return buildEventByServiceAuthenticationMethod(context, registeredService, authenticationResultBuilder);
                }
            }
            return grantTicketGrantingTicketToAuthenticationResult(context, authenticationResultBuilder, service);
        } catch (final AuthenticationException e) {
            logger.debug(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        } catch (final Exception e) {
            logger.debug(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
        }
    }

    /**
     * Build event by service authentication method event.
     *
     * @param context              the context
     * @param service              the service
     * @param builder              the builder
     * @return the event
     */
    protected Event buildEventByServiceAuthenticationMethod(final RequestContext context, final RegisteredService service,
                                                            final AuthenticationResultBuilder builder) {
        logger.debug("Attempting to build an event based on the authentication method [{}] and service [{}]",
                service.getAuthenticationPolicy().getAuthenticationMethod(), service.getName());

        final Event event = new Event(this, service.getAuthenticationPolicy().getAuthenticationMethod());
        logger.debug("Resulting event id is [{}]. Locating transitions in the context for that event id...",
                event.getId());

        final TransitionDefinition def = context.getMatchingTransition(event.getId());
        if (def == null) {
            logger.warn("Transition definition cannot be found for event [{}]", event.getId());
            throw new UnrecognizedAuthenticationMethodException(service.getAuthenticationPolicy().getAuthenticationMethod());
        }
        logger.debug("Found matching transition [{}] with target [{}] for event [{}].",
                def.getId(), def.getTargetStateId(), event.getId());

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(builder.getInitialAuthentication(), context);
        return event;
    }


}
