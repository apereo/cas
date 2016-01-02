package org.jasig.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationContextBuilder;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.MessageDescriptor;
import org.jasig.cas.authentication.UnrecognizedAuthenticationMethodException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * This is {@link PrimaryAuthenticationContextWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("primaryAuthenticationContextWebflowEventResolver")
public class PrimaryAuthenticationContextWebflowEventResolver implements AuthenticationContextWebflowEventResolver {
    /**
     * Authentication succeeded with warnings from authn subsystem that should be displayed to user.
     */
    private static final String SUCCESS_WITH_WARNINGS = "successWithWarnings";

    /**
     * The Constant MFA_EVENT_ID_PREFIX.
     */
    private static final String MFA_EVENT_ID_PREFIX = "mfa-";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @NotNull
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;


    @Override
    public Event resolve(final AuthenticationContextBuilder authenticationContextBuilder, final RequestContext context,
                         final MessageContext messageContext) throws Exception {

        final Service service = WebUtils.getService(context);
        final AuthenticationContext authenticationContext = authenticationContextBuilder.build(service);
        if (authenticationContext.getService() != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(authenticationContext.getService());
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service access is disallowed");
            }
            if (StringUtils.isNotBlank(registeredService.getAuthenticationPolicy().getAuthenticationMethod())) {
                return buildEventByServiceAuthenticationMethod(context, registeredService,
                        authenticationContextBuilder, authenticationContext);
            }
        }
        final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        if (addWarningMessagesToMessageContextIfNeeded(tgt, messageContext)) {
            return newEvent(SUCCESS_WITH_WARNINGS);
        }

        return newEvent(AbstractCasWebflowConfigurer.TRANSITION_ID_SUCCESS);
    }

    /**
     * Add warning messages to message context if needed.
     *
     * @param tgtId          the tgt id
     * @param messageContext the message context
     * @return true if warnings were found and added, false otherwise.
     * @since 4.1.0
     */
    protected boolean addWarningMessagesToMessageContextIfNeeded(final TicketGrantingTicket tgtId, final MessageContext messageContext) {
        boolean foundAndAddedWarnings = false;
        for (final Map.Entry<String, HandlerResult> entry : tgtId.getAuthentication().getSuccesses().entrySet()) {
            for (final MessageDescriptor message : entry.getValue().getWarnings()) {
                addWarningToContext(messageContext, message);
                foundAndAddedWarnings = true;
            }
        }
        return foundAndAddedWarnings;

    }

    private Event buildEventByServiceAuthenticationMethod(final RequestContext context, final RegisteredService service,
                                                          final AuthenticationContextBuilder builder,
                                                          final AuthenticationContext authenticationContext) {
        logger.debug("Attempting to build an event based on the authentication method [{}] and service [{}]",
                service.getAuthenticationPolicy().getAuthenticationMethod(), service.getName());

        final Event event = new Event(this, MFA_EVENT_ID_PREFIX + service.getAuthenticationPolicy().getAuthenticationMethod());
        logger.debug("Resulting event id is [{}]. Locating transitions in the context for that event id...",
                event.getId());

        final TransitionDefinition def = context.getMatchingTransition(event.getId());
        if (def == null) {
            logger.warn("Transition definition cannot be found for event [{}]", event.getId());
            throw new UnrecognizedAuthenticationMethodException(service.getAuthenticationPolicy().getAuthenticationMethod());
        }
        logger.debug("Found matching transition [{}] with target [{}] for event [{}].",
                def.getId(), def.getTargetStateId(), event.getId());

        WebUtils.putAuthenticationContextBuilder(builder, context);
        WebUtils.putAuthenticationContext(authenticationContext, context);
        return event;
    }

    /**
     * Adds a warning message to the message context.
     *
     * @param context Message context.
     * @param warning Warning message.
     */
    private static void addWarningToContext(final MessageContext context, final MessageDescriptor warning) {
        final MessageBuilder builder = new MessageBuilder()
                .warning()
                .code(warning.getCode())
                .defaultText(warning.getDefaultMessage())
                .args(warning.getParams());
        context.addMessage(builder.build());
    }

    /**
     * New event based on the given id.
     *
     * @param id the id
     * @return the event
     */
    protected Event newEvent(final String id) {
        return new Event(this, id);
    }
}
