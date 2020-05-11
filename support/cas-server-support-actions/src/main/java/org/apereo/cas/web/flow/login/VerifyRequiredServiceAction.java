package org.apereo.cas.web.flow.login;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class to automatically set the paths for the CookieGenerators.
 * <p>
 * Note: This is technically not thread-safe, but because its overriding with a
 * constant value it doesn't matter.
 * <p>
 * Note: As of CAS 3.1, this is a required class that retrieves and exposes the
 * values in the two cookies for subclasses to use.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@RequiredArgsConstructor
@Getter
public class VerifyRequiredServiceAction extends AbstractAction {
    private final ServicesManager servicesManager;
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;
    private final CasConfigurationProperties casProperties;
    private final TicketRegistrySupport ticketRegistrySupport;

    @Override
    public Event doExecute(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val ticketGrantingTicketId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);

        val initialService = casProperties.getSso().getRequiredServicePattern();
        if (StringUtils.isNotBlank(initialService)) {
            val initialServicePattern = RegexUtils.createPattern(initialService);
            enforceInitialMandatoryService(context, ticketGrantingTicketId, initialServicePattern);
        }
        return success();
    }

    /**
     * Configure webflow for initial mandatory service.
     *
     * @param context                the context
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @param initialServicePattern  the initial service pattern
     */
    protected void enforceInitialMandatoryService(final RequestContext context,
                                                  final String ticketGrantingTicketId,
                                                  final Pattern initialServicePattern) {
        if (shouldSkipRequiredServiceCheck(context, initialServicePattern)) {
            return;
        }

        val servicesToMatch = collectServicesToMatch(context, ticketGrantingTicketId);
        if (!servicesToMatch.isEmpty()) {
            val matches = servicesToMatch
                .stream()
                .anyMatch(service -> RegexUtils.find(initialServicePattern, service.getId()));
            if (!matches) {
                throw new NoSuchFlowExecutionException(context.getFlowExecutionContext().getKey(),
                    new UnauthorizedServiceException("screen.service.initial.message", "Service is required"));
            }
        }
    }

    /**
     * Should skip required service check.
     *
     * @param context               the context
     * @param initialServicePattern the initial service pattern
     * @return true/false
     */
    protected boolean shouldSkipRequiredServiceCheck(final RequestContext context,
                                                   final Pattern initialServicePattern) {
        val service = WebUtils.getService(context);
        if (service == null) {
            return false;
        }
        val matches = RegexUtils.find(initialServicePattern, service.getId());
        if (matches) {
            return true;
        }

        val registeredService = this.servicesManager.findServiceBy(service);
        return registeredService != null && RegisteredServiceProperties.SKIP_REQUIRED_SERVICE_CHECK.isAssignedTo(registeredService);
    }

    /**
     * Collect services to match.
     *
     * @param context                the context
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @return the list
     */
    protected List<Service> collectServicesToMatch(final RequestContext context, final String ticketGrantingTicketId) {
        val servicesToMatch = new ArrayList<Service>();

        val ticket = StringUtils.isNotBlank(ticketGrantingTicketId)
            ? ticketRegistrySupport.getTicketGrantingTicket(ticketGrantingTicketId)
            : null;

        if (ticket != null) {
            servicesToMatch.addAll(ticket.getServices().values());
        }
        return servicesToMatch;
    }

}
