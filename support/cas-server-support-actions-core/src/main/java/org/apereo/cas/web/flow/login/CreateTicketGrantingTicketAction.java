package org.apereo.cas.web.flow.login;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
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
    private final CasWebflowEventResolutionConfigurationContext configurationContext;

    @Override
    public Event doExecute(final RequestContext context) {
        val service = WebUtils.getService(context);
        val registeredService = WebUtils.getRegisteredService(context);
        val authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(context);

        LOGGER.trace("Finalizing authentication transactions and issuing ticket-granting ticket");
        val authenticationResult = configurationContext.getAuthenticationSystemSupport()
            .finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        LOGGER.trace("Finalizing authentication event...");
        val authentication = buildFinalAuthentication(authenticationResult);
        val ticketGrantingTicket = determineTicketGrantingTicketId(context);
        LOGGER.debug("Creating ticket-granting ticket, potentially based on [{}]", ticketGrantingTicket);
        val tgt = createOrUpdateTicketGrantingTicket(authenticationResult, authentication, ticketGrantingTicket);

        if (registeredService != null && registeredService.getAccessStrategy() != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
        }
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthenticationResult(authenticationResult, context);
        WebUtils.putAuthentication(tgt.getAuthentication(), context);

        LOGGER.trace("Calculating authentication warning messages...");
        val warnings = calculateAuthenticationWarningMessages(tgt, context.getMessageContext());
        if (!warnings.isEmpty()) {
            val attributes = new LocalAttributeMap<Object>(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, warnings);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, attributes);
        }
        return success();
    }

    /**
     * Add warning messages to message context if needed.
     *
     * @param tgtId          the tgt id
     * @param messageContext the message context
     * @return authn warnings from all handlers and results
     * @since 4.1.0
     */
    private static Collection<MessageDescriptor> calculateAuthenticationWarningMessages(final TicketGrantingTicket tgtId,
                                                                                        final MessageContext messageContext) {
        val entries = tgtId.getAuthentication().getSuccesses().entrySet();
        val messages = entries
            .stream()
            .map(entry -> entry.getValue().getWarnings())
            .filter(entry -> !entry.isEmpty())
            .collect(Collectors.toList());
        messages.add(tgtId.getAuthentication().getWarnings());

        return messages
            .stream()
            .flatMap(Collection::stream)
            .peek(message -> addMessageDescriptorToMessageContext(messageContext, message))
            .collect(Collectors.toSet());
    }

    private static boolean areAuthenticationsEssentiallyEqual(final Authentication auth1, final Authentication auth2) {
        if (auth1 == null || auth2 == null) {
            return false;
        }

        val builder = new EqualsBuilder();
        builder.append(auth1.getPrincipal(), auth2.getPrincipal());
        builder.append(auth1.getCredentials(), auth2.getCredentials());
        builder.append(auth1.getSuccesses(), auth2.getSuccesses());
        builder.append(auth1.getAttributes(), auth2.getAttributes());
        return builder.isEquals();
    }

    private String determineTicketGrantingTicketId(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val ticketGrantingTicketId = configurationContext.getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request);
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return WebUtils.getTicketGrantingTicketId(context);
        }
        return ticketGrantingTicketId;
    }

    private boolean shouldIssueTicketGrantingTicket(final Authentication authentication, final String ticketGrantingTicket) {
        if (StringUtils.isBlank(ticketGrantingTicket)) {
            return true;
        }
        LOGGER.trace("Located ticket-granting ticket in the context. Retrieving associated authentication");
        val authenticationFromTgt = configurationContext.getTicketRegistrySupport().getAuthenticationFrom(ticketGrantingTicket);

        if (authenticationFromTgt == null) {
            LOGGER.debug("Authentication session associated with [{}] is no longer valid", ticketGrantingTicket);
            configurationContext.getCentralAuthenticationService().deleteTicket(ticketGrantingTicket);
            return true;
        }

        if (areAuthenticationsEssentiallyEqual(authentication, authenticationFromTgt)) {
            LOGGER.debug("Resulting authentication matches the authentication from context");
            return false;
        }
        LOGGER.debug("Resulting authentication is different from the context");
        return true;
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
                if (StringUtils.isNotBlank(ticketGrantingTicket)) {
                    LOGGER.trace("Removing existing ticket-granting ticket [{}]", ticketGrantingTicket);
                    configurationContext.getTicketRegistry().deleteTicket(ticketGrantingTicket);
                }

                LOGGER.trace("Attempting to issue a new ticket-granting ticket...");
                return configurationContext.getCentralAuthenticationService().createTicketGrantingTicket(authenticationResult);
            }
            LOGGER.debug("Updating the existing ticket-granting ticket [{}]...", ticketGrantingTicket);
            val tgt = configurationContext.getCentralAuthenticationService().getTicket(ticketGrantingTicket, TicketGrantingTicket.class);
            tgt.getAuthentication().update(authentication);
            configurationContext.getCentralAuthenticationService().updateTicket(tgt);
            return tgt;
        } catch (final PrincipalException e) {
            LoggingUtils.error(LOGGER, e);
            throw e;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new InvalidTicketException(ticketGrantingTicket);
        }
    }
}
