package org.apereo.cas.web.flow.login;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Action that handles the {@link TicketGrantingTicket} creation and destruction. If the
 * action is given a {@link TicketGrantingTicket} and one also already exists, the old
 * one is destroyed and replaced with the new one conditionally.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CreateTicketGrantingTicketAction extends BaseCasWebflowAction {
    private final CasWebflowEventResolutionConfigurationContext configurationContext;

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        val service = WebUtils.getService(context);
        val registeredService = WebUtils.getRegisteredService(context);
        val authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(context);

        LOGGER.trace("Finalizing authentication transactions and issuing ticket-granting ticket");
        val authenticationResult = FunctionUtils.doUnchecked(() -> configurationContext.getAuthenticationSystemSupport()
            .finalizeAllAuthenticationTransactions(authenticationResultBuilder, service));
        LOGGER.trace("Finalizing authentication event...");
        val authentication = buildFinalAuthentication(authenticationResult);
        val ticketGrantingTicketId = determineTicketGrantingTicketId(context);
        LOGGER.debug("Creating ticket-granting ticket, potentially based on [{}]", ticketGrantingTicketId);
        val ticketGrantingTicket = configurationContext.getSingleSignOnBuildingStrategy()
            .buildTicketGrantingTicket(authenticationResult, authentication, ticketGrantingTicketId);

        if (registeredService != null && registeredService.getAccessStrategy() != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
        }
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        WebUtils.putAuthenticationResult(authenticationResult, context);
        WebUtils.putAuthentication(ticketGrantingTicket, context);

        LOGGER.trace("Calculating authentication warning messages...");
        val warnings = calculateAuthenticationWarningMessages(context);
        if (!warnings.isEmpty()) {
            val attributes = new LocalAttributeMap<Object>(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, warnings);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, attributes);
        }
        return success();
    }

    protected Authentication buildFinalAuthentication(final AuthenticationResult authenticationResult) {
        return authenticationResult.getAuthentication();
    }

    protected String determineTicketGrantingTicketId(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val ticketGrantingTicketId = configurationContext.getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request);
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return WebUtils.getTicketGrantingTicketId(context);
        }
        return ticketGrantingTicketId;
    }
    
    private static Collection<? extends MessageDescriptor> calculateAuthenticationWarningMessages(final RequestContext context) {
        val ticketGrantingTicket = WebUtils.getTicketGrantingTicket(context);
        return Optional.ofNullable(ticketGrantingTicket)
            .filter(AuthenticationAwareTicket.class::isInstance)
            .map(AuthenticationAwareTicket.class::cast)
            .map(tgt -> {
                val authentication = tgt.getAuthentication();
                val messages = authentication.getSuccesses().entrySet();
                val warnings = messages
                    .stream()
                    .map(entry -> entry.getValue().getWarnings())
                    .filter(entry -> !entry.isEmpty())
                    .collect(Collectors.toList());
                warnings.add(authentication.getWarnings());
                return warnings
                    .stream()
                    .flatMap(Collection::stream)
                    .peek(message -> addMessageDescriptorToMessageContext(context.getMessageContext(), message))
                    .collect(Collectors.<MessageDescriptor>toSet());
            })
            .orElseGet(HashSet::new);
    }

    protected static void addMessageDescriptorToMessageContext(final MessageContext context, final MessageDescriptor warning) {
        WebUtils.addWarningMessageToContext(context, warning);
    }
}
