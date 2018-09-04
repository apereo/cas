package org.apereo.cas.web.flow.login;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Action that handles the {@link TicketGrantingTicket} creation and destruction. If the
 * action is given a {@link TicketGrantingTicket} and one also already exists, the old
 * one is destroyed and replaced with the new one. This action always returns
 * "success".
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CreateTicketGrantingTicketAction extends AbstractAction {

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final TicketRegistrySupport ticketRegistrySupport;

    @Override
    public Event doExecute(final RequestContext context) {
        final Service service = WebUtils.getService(context);
        final RegisteredService registeredService = WebUtils.getRegisteredService(context);
        final AuthenticationResultBuilder authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(context);

        LOGGER.debug("Finalizing authentication transactions and issuing ticket-granting ticket");
        final AuthenticationResult authenticationResult = this.authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        LOGGER.debug("Finalizing authentication event...");
        final Authentication authentication = buildFinalAuthentication(authenticationResult);
        final String ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.debug("Creating ticket-granting ticket, potentially based on [{}]", ticketGrantingTicket);
        final TicketGrantingTicket tgt = createOrUpdateTicketGrantingTicket(authenticationResult, authentication, ticketGrantingTicket);

        if (registeredService != null && registeredService.getAccessStrategy() != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
        }
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthenticationResult(authenticationResult, context);
        WebUtils.putAuthentication(tgt.getAuthentication(), context);

        LOGGER.debug("Calculating authentication warning messages...");
        final Collection<MessageDescriptor> warnings = calculateAuthenticationWarningMessages(tgt, context.getMessageContext());
        if (!warnings.isEmpty()) {
            final LocalAttributeMap attributes = new LocalAttributeMap(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, warnings);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, attributes);
        }
        return success();
    }

    /**
     * Build final authentication authentication.
     *
     * @param authenticationResult the authentication result
     * @return the authentication
     */
    protected Authentication buildFinalAuthentication(final AuthenticationResult authenticationResult) {
        return authenticationResult.getAuthentication();
    }

    /**
     * Create or update ticket granting ticket ticket granting ticket.
     *
     * @param authenticationResult the authentication result
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the ticket granting ticket
     */
    protected TicketGrantingTicket createOrUpdateTicketGrantingTicket(final AuthenticationResult authenticationResult,
                                                                      final Authentication authentication, final String ticketGrantingTicket) {
        final TicketGrantingTicket tgt;
        if (shouldIssueTicketGrantingTicket(authentication, ticketGrantingTicket)) {
            LOGGER.debug("Attempting to issue a new ticket-granting ticket...");
            tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
        } else {
            LOGGER.debug("Updating the existing ticket-granting ticket [{}]...", ticketGrantingTicket);
            tgt = this.centralAuthenticationService.getTicket(ticketGrantingTicket, TicketGrantingTicket.class);
            tgt.getAuthentication().update(authentication);
            this.centralAuthenticationService.updateTicket(tgt);
        }
        return tgt;
    }

    private boolean shouldIssueTicketGrantingTicket(final Authentication authentication, final String ticketGrantingTicket) {
        boolean issueTicketGrantingTicket = true;
        if (StringUtils.isNotBlank(ticketGrantingTicket)) {
            LOGGER.debug("Located ticket-granting ticket in the context. Retrieving associated authentication");
            final Authentication authenticationFromTgt = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
            if (authenticationFromTgt == null) {
                LOGGER.debug("Authentication session associated with [{}] is no longer valid", ticketGrantingTicket);
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            } else if (areAuthenticationsEssentiallyEqual(authentication, authenticationFromTgt)) {
                LOGGER.debug("Resulting authentication matches the authentication from context");
                issueTicketGrantingTicket = false;
            } else {
                LOGGER.debug("Resulting authentication is different from the context");
            }
        }
        return issueTicketGrantingTicket;
    }

    private boolean areAuthenticationsEssentiallyEqual(final Authentication auth1, final Authentication auth2) {
        if ((auth1 == null && auth2 != null) || (auth1 != null && auth2 == null)) {
            return false;
        }
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(auth1.getPrincipal(), auth2.getPrincipal());
        builder.append(auth1.getCredentials(), auth2.getCredentials());
        builder.append(auth1.getSuccesses(), auth2.getSuccesses());
        builder.append(auth1.getAttributes(), auth2.getAttributes());
        return builder.isEquals();
    }

    /**
     * Add warning messages to message context if needed.
     *
     * @param tgtId          the tgt id
     * @param messageContext the message context
     * @return authn warnings from all handlers and results
     * @since 4.1.0
     */
    private static Collection<MessageDescriptor> calculateAuthenticationWarningMessages(final TicketGrantingTicket tgtId, final MessageContext messageContext) {
        final Set<Map.Entry<String, AuthenticationHandlerExecutionResult>> entries = tgtId.getAuthentication().getSuccesses().entrySet();
        return entries
            .stream()
            .map(entry -> entry.getValue().getWarnings())
            .flatMap(Collection::stream)
            .map(message -> {
                addMessageDescriptorToMessageContext(messageContext, message);
                return message;
            })
            .collect(Collectors.toSet());
    }

    /**
     * Adds a warning message to the message context.
     *
     * @param context Message context.
     * @param warning Warning message.
     */
    protected static void addMessageDescriptorToMessageContext(final MessageContext context, final MessageDescriptor warning) {
        final MessageBuilder builder = new MessageBuilder()
            .warning()
            .code(warning.getCode())
            .defaultText(warning.getDefaultMessage())
            .args((Object[]) warning.getParams());
        context.addMessage(builder.build());
    }
}
