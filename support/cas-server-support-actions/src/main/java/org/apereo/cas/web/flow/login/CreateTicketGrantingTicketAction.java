package org.apereo.cas.web.flow.login;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    /**
     * Add warning messages to message context if needed.
     *
     * @param tgtId          the tgt id
     * @param messageContext the message context
     * @return authn warnings from all handlers and results
     * @since 4.1.0
     */
    private static Collection<MessageDescriptor> calculateAuthenticationWarningMessages(final TicketGrantingTicket tgtId, final MessageContext messageContext) {
        val entries = tgtId.getAuthentication().getSuccesses().entrySet();
        return entries
            .stream()
            .map(entry -> entry.getValue().getWarnings())
            .flatMap(Collection::stream)
            .peek(message -> addMessageDescriptorToMessageContext(messageContext, message))
            .collect(Collectors.toSet());
    }

    /**
     * Adds a warning message to the message context.
     *
     * @param context Message context.
     * @param warning Warning message.
     */
    protected static void addMessageDescriptorToMessageContext(final MessageContext context, final MessageDescriptor warning) {
        val builder = new MessageBuilder()
            .warning()
            .code(warning.getCode())
            .defaultText(warning.getDefaultMessage())
            .args((Object[]) warning.getParams());
        context.addMessage(builder.build());
    }

    @Override
    public Event doExecute(final RequestContext context) {
        val service = WebUtils.getService(context);
        val registeredService = WebUtils.getRegisteredService(context);
        val authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(context);

        LOGGER.debug("Finalizing authentication transactions and issuing ticket-granting ticket");
        val authenticationResult = this.authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        LOGGER.debug("Finalizing authentication event...");
        val authentication = buildFinalAuthentication(authenticationResult);
        val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.debug("Creating ticket-granting ticket, potentially based on [{}]", ticketGrantingTicket);
        val tgt = createOrUpdateTicketGrantingTicket(authenticationResult, authentication, ticketGrantingTicket);

        if (registeredService != null && registeredService.getAccessStrategy() != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
        }
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthenticationResult(authenticationResult, context);
        WebUtils.putAuthentication(tgt.getAuthentication(), context);

        LOGGER.debug("Calculating authentication warning messages...");
        val warnings = calculateAuthenticationWarningMessages(tgt, context.getMessageContext());
        if (!warnings.isEmpty()) {
            val attributes = new LocalAttributeMap(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, warnings);
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
        try {
            if (shouldIssueTicketGrantingTicket(authentication, ticketGrantingTicket)) {
                LOGGER.debug("Attempting to issue a new ticket-granting ticket...");
                return this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
            }
            LOGGER.debug("Updating the existing ticket-granting ticket [{}]...", ticketGrantingTicket);
            val tgt = this.centralAuthenticationService.getTicket(ticketGrantingTicket, TicketGrantingTicket.class);
            tgt.getAuthentication().update(authentication);
            this.centralAuthenticationService.updateTicket(tgt);
            return tgt;
        } catch (final PrincipalException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new InvalidTicketException(ticketGrantingTicket);
        }
    }

    private boolean shouldIssueTicketGrantingTicket(final Authentication authentication, final String ticketGrantingTicket) {
        if (StringUtils.isBlank(ticketGrantingTicket)) {
            return true;
        }
        LOGGER.debug("Located ticket-granting ticket in the context. Retrieving associated authentication");
        val authenticationFromTgt = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);

        if (authenticationFromTgt == null) {
            LOGGER.debug("Authentication session associated with [{}] is no longer valid", ticketGrantingTicket);
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            return true;
        }

        if (areAuthenticationsEssentiallyEqual(authentication, authenticationFromTgt)) {
            LOGGER.debug("Resulting authentication matches the authentication from context");
            return false;
        }
        LOGGER.debug("Resulting authentication is different from the context");
        return true;
    }

    private boolean areAuthenticationsEssentiallyEqual(final Authentication auth1, final Authentication auth2) {
        if (auth1 == null && auth2 == null) {
            return false;
        }
        if ((auth1 == null && auth2 != null) || (auth1 != null && auth2 == null)) {
            return false;
        }
        val builder = new EqualsBuilder();
        builder.append(auth1.getPrincipal(), auth2.getPrincipal());
        builder.append(auth1.getCredentials(), auth2.getCredentials());
        builder.append(auth1.getSuccesses(), auth2.getSuccesses());
        builder.append(auth1.getAttributes(), auth2.getAttributes());
        return builder.isEquals();
    }
}
