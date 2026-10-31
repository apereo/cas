package org.apereo.cas.web.flow.account;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasEventRepository;
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
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;

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

    private static final int SIGN_IN_ACTIVITY_DAYS = 30;

    private static final int SIGN_IN_ACTIVITY_LIMIT = 20;

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    private final AuditTrailExecutionPlan auditTrailManager;

    private final GeoLocationService geoLocationService;

    private final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    private final @Nullable CasEventRepository casEventRepository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
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
            buildSignInActivity(requestContext, ticket);
            buildPasswordExpiration(requestContext, ticket);
        });
        return success();
    }

    protected void buildActiveSingleSignOnSessions(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        try (val sessions = ticketRegistry.getSessionsFor(ticket.getAuthentication().getPrincipal().getId())) {
            val activeSessions = sessions
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
    }

    protected void buildAuthorizedServices(final RequestContext requestContext, final TicketGrantingTicket ticket,
                                           final WebApplicationService service) {
        val authzAttributes = (Map) CollectionUtils.merge(ticket.getAuthentication().getAttributes(),
            ticket.getAuthentication().getPrincipal().getAttributes());
        val authorizedServices = servicesManager.getAllServices()
            .stream()
            .filter(registeredService -> !registeredService.isInternal())
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

    /**
     * Put recent sign-in attempts of the principal, as recorded by the CAS event repository, into the flow scope.
     *
     * @param requestContext the request context
     * @param ticket         the ticket granting ticket
     */
    protected void buildSignInActivity(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        val repository = casEventRepository;
        if (repository != null && BeanSupplier.isNotProxy(repository)) {
            val since = ZonedDateTime.now(ZoneOffset.UTC).minusDays(SIGN_IN_ACTIVITY_DAYS);
            val activity = FunctionUtils.doAndHandle(
                () -> {
                    try (val events = repository.getEventsForPrincipal(ticket.getAuthentication().getPrincipal().getId(), since)) {
                        return events
                            .filter(event -> AccountSignInActivity.OUTCOMES.containsKey(event.getType()))
                            .map(AccountSignInActivity::from)
                            .sorted(Comparator.comparing(AccountSignInActivity::getWhen).reversed())
                            .limit(SIGN_IN_ACTIVITY_LIMIT)
                            .toList();
                    }
                },
                throwable -> List.<AccountSignInActivity>of()).get();
            FunctionUtils.doIf(!activity.isEmpty(), u -> requestContext.getFlowScope().put("signInActivity", activity)).accept(activity);
        }
    }

    /**
     * Put the number of days until the password expires, and the date it expires,
     * into the flow scope when the authentication carries a password expiration warning.
     *
     * @param requestContext the request context
     * @param ticket         the ticket granting ticket
     */
    protected void buildPasswordExpiration(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        val authentication = ticket.getAuthentication();
        Stream.concat(authentication.getWarnings().stream(),
                authentication.getSuccesses().values().stream().map(AuthenticationHandlerExecutionResult::getWarnings).flatMap(List::stream))
            .filter(PasswordExpiringWarningMessageDescriptor.class::isInstance)
            .map(PasswordExpiringWarningMessageDescriptor.class::cast)
            .findFirst()
            .ifPresent(warning -> {
                requestContext.getFlowScope().put("passwordExpirationDays", warning.getDaysToExpiration());
                requestContext.getFlowScope().put("passwordExpirationDate",
                    authentication.getAuthenticationDate().plusDays(warning.getDaysToExpiration()));
            });
    }
}
