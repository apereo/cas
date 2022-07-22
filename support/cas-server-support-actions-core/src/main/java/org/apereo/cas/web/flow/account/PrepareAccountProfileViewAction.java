package org.apereo.cas.web.flow.account;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ISOStandardDateFormat;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prepare the authenticated account for view.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class PrepareAccountProfileViewAction extends BaseCasWebflowAction {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    private final AuditTrailExecutionPlan auditTrailManager;

    private final TicketSerializationManager ticketSerializationManager;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        val ticketGrantingTicket = FunctionUtils.doAndHandle(
            () -> Optional.of(ticketRegistry.getTicket(tgt, TicketGrantingTicket.class)),
            throwable -> Optional.<TicketGrantingTicket>empty()).get();

        ticketGrantingTicket.ifPresent(ticket -> {
            WebUtils.putAuthentication(ticket.getAuthentication(), requestContext);
            val service = WebUtils.getService(requestContext);
            if (casProperties.getView().isAuthorizedServicesOnSuccessfulLogin()) {
                buildAuthorizedServices(requestContext, ticket, service);
            }
            buildAuditLogRecords(requestContext, ticket);
            buildActiveSingleSignOnSessions(requestContext, ticket);
        });

        return success();
    }

    protected void buildActiveSingleSignOnSessions(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        val activeSessions = ticketRegistry.getSessionsFor(ticket.getAuthentication().getPrincipal().getId())
            .map(TicketGrantingTicket.class::cast)
            .map(SingleSignOnSession::new)
            .collect(Collectors.toList());
        WebUtils.putSingleSignOnSessions(requestContext, activeSessions);
    }

    protected void buildAuthorizedServices(final RequestContext requestContext, final TicketGrantingTicket ticket,
                                           final WebApplicationService service) {
        val authzAttributes = (Map) CollectionUtils.merge(ticket.getAuthentication().getAttributes(),
            ticket.getAuthentication().getPrincipal().getAttributes());
        val authorizedServices = servicesManager.getAllServices()
            .stream()
            .filter(registeredService -> FunctionUtils.doAndHandle(
                () -> RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, ticket.getAuthentication().getPrincipal().getId(), authzAttributes),
                throwable -> false).get())
            .collect(Collectors.toList());
        WebUtils.putAuthorizedServices(requestContext, authorizedServices);
    }

    protected void buildAuditLogRecords(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        val sinceDate = LocalDate.now(Clock.systemUTC()).minusMonths(2);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, sinceDate,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, ticket.getAuthentication().getPrincipal().getId());
        val auditLog = auditTrailManager.getAuditRecords(criteria)
            .stream()
            .sorted(Comparator.comparing(AuditActionContext::getWhenActionWasPerformed).reversed())
            .map(AccountAuditActionContext::new)
            .collect(Collectors.toList());
        FunctionUtils.doIf(!auditLog.isEmpty(), u -> requestContext.getFlowScope().put("auditLog", auditLog)).accept(auditLog);
    }

    @Getter
    @SuppressWarnings("UnusedMethod")
    private static class AccountAuditActionContext extends AuditActionContext {
        private static final long serialVersionUID = 8935451143814878214L;

        private final String json;

        AccountAuditActionContext(final AuditActionContext context) {
            super(context.getPrincipal(), context.getResourceOperatedUpon(), context.getActionPerformed(),
                context.getApplicationCode(), context.getWhenActionWasPerformed(), context.getClientIpAddress(),
                context.getServerIpAddress(), context.getServerIpAddress());
            this.json = FunctionUtils.doUnchecked(() -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
        }
    }

    @Getter
    @SuppressWarnings("UnusedMethod")
    class SingleSignOnSession implements Serializable {
        private static final long serialVersionUID = 8935451143814878214L;

        private final String payload;

        private final String principal;

        private final String authenticationDate;

        SingleSignOnSession(final TicketGrantingTicket ticket) {
            this.payload = ticketSerializationManager.serializeTicket(ticket);
            this.principal = ticket.getAuthentication().getPrincipal().getId();

            val dateFormat = new ISOStandardDateFormat();
            this.authenticationDate = dateFormat.format(DateTimeUtils.dateOf(ticket.getAuthentication().getAuthenticationDate()));
        }
    }
}
