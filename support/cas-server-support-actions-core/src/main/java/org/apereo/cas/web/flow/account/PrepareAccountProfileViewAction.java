package org.apereo.cas.web.flow.account;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.jooq.lambda.Unchecked;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDateTime;
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
@Transactional(transactionManager = TicketRegistry.TICKET_TRANSACTION_MANAGER)
public class PrepareAccountProfileViewAction extends BaseCasWebflowAction {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    private final AuditTrailExecutionPlan auditTrailManager;

    private final GeoLocationService geoLocationService;

    private final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        val ticketGrantingTicket = FunctionUtils.doAndHandle(
            () -> Optional.of(ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class)),
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
            .map(tgt -> {
                val ssoSession = new AccountSingleSignOnSession(tgt);
                ssoSession.setGeoLocation(FunctionUtils.doIf(BeanSupplier.isNotProxy(geoLocationService),
                    () -> geoLocationService.locate(ssoSession.getClientIpAddress()).build(), () -> "N/A").get());
                ssoSession.setPayload(FunctionUtils.doUnchecked(() -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(ssoSession)));
                return ssoSession;
            })
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
                () -> principalAccessStrategyEnforcer.authorize(
                    RegisteredServicePrincipalAccessStrategyEnforcer.PrincipalAccessStrategyContext.builder()
                        .registeredService(registeredService)
                        .principalId(ticket.getAuthentication().getPrincipal().getId())
                        .principalAttributes(authzAttributes)
                        .service(service)
                        .applicationContext(requestContext.getActiveFlow().getApplicationContext())
                        .build()),
                throwable -> false).get())
            .sorted()
            .collect(Collectors.toList());
        WebUtils.putAuthorizedServices(requestContext, authorizedServices);
    }

    protected void buildAuditLogRecords(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        val sinceDate = LocalDateTime.now(Clock.systemUTC()).minusMonths(2);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, sinceDate,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, ticket.getAuthentication().getPrincipal().getId());
        val auditLog = auditTrailManager.getAuditRecords(criteria)
            .stream()
            .sorted(Comparator.comparing(AuditActionContext::getWhenActionWasPerformed).reversed())
            .map(Unchecked.function(entry -> new AccountAuditActionContext(entry, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(entry))))
            .collect(Collectors.toList());
        FunctionUtils.doIf(!auditLog.isEmpty(), u -> requestContext.getFlowScope().put("auditLog", auditLog)).accept(auditLog);
    }

}
