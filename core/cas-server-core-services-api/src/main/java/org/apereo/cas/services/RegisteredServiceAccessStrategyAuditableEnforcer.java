package org.apereo.cas.services;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

import java.util.Optional;

/**
 * This is {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RegisteredServiceAccessStrategyAuditableEnforcer extends BaseAuditableExecution {
    private final WatchableGroovyScriptResource accessStrategyScriptResource;

    public RegisteredServiceAccessStrategyAuditableEnforcer(final CasConfigurationProperties casProperties) {
        val location = casProperties.getAccessStrategy().getGroovy().getLocation();
        this.accessStrategyScriptResource = location != null
            ? new WatchableGroovyScriptResource(location)
            : null;
    }

    private static Optional<AuditableExecutionResult> byServiceTicketAndAuthnResultAndRegisteredService(final AuditableContext context) {
        val providedRegisteredService = context.getRegisteredService();
        if (context.getServiceTicket().isPresent() && context.getAuthenticationResult().isPresent()
            && providedRegisteredService.isPresent()) {
            val result = AuditableExecutionResult.of(context);
            try {
                val serviceTicket = context.getServiceTicket().orElseThrow();
                val authResult = context.getAuthenticationResult().orElseThrow();
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(serviceTicket,
                    authResult, providedRegisteredService.get());
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private static Optional<AuditableExecutionResult> byServiceAndRegisteredServiceAndTicketGrantingTicket(final AuditableContext context) {
        val providedService = context.getService();
        val providedRegisteredService = context.getRegisteredService();
        val ticketGrantingTicket = context.getTicketGrantingTicket();
        if (providedService.isPresent() && providedRegisteredService.isPresent()
            && ticketGrantingTicket.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();
            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .ticketGrantingTicket(ticketGrantingTicket.get())
                .build();
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, ticketGrantingTicket.get());
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private static Optional<AuditableExecutionResult> byRegisteredService(final AuditableContext context) {
        val providedRegisteredService = context.getRegisteredService();
        if (providedRegisteredService.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .build();
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private static Optional<AuditableExecutionResult> byServiceAndRegisteredService(final AuditableContext context) {
        val providedService = context.getService();
        val providedRegisteredService = context.getRegisteredService();
        if (providedService.isPresent() && providedRegisteredService.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();

            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .build();
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private static Optional<AuditableExecutionResult> byServiceAndRegisteredServiceAndAuthentication(final AuditableContext context) {
        val providedService = context.getService();
        val providedRegisteredService = context.getRegisteredService();
        val providedAuthn = context.getAuthentication();
        if (providedService.isPresent() && providedRegisteredService.isPresent() && providedAuthn.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();
            val authentication = providedAuthn.get();

            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .authentication(authentication)
                .build();

            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, authentication);
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    @Override
    @Audit(action = AuditableActions.SERVICE_ACCESS_ENFORCEMENT,
        actionResolverName = AuditActionResolvers.SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER)
    public AuditableExecutionResult execute(final AuditableContext context) {
        return byExternalGroovyScript(context)
            .or(() -> byServiceTicketAndAuthnResultAndRegisteredService(context))
            .or(() -> byServiceAndRegisteredServiceAndTicketGrantingTicket(context))
            .or(() -> byServiceAndRegisteredServiceAndAuthentication(context))
            .or(() -> byServiceAndRegisteredService(context))
            .or(() -> byRegisteredService(context))
            .orElseGet(() -> {
                val result = AuditableExecutionResult.builder().build();
                result.setException(new UnauthorizedServiceException(
                    UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service unauthorized"));
                return result;
            });
    }

    /**
     * By external groovy script optional.
     *
     * @param context the context
     * @return the optional
     */
    protected Optional<AuditableExecutionResult> byExternalGroovyScript(final AuditableContext context) {
        if (accessStrategyScriptResource != null) {
            val args = new Object[]{context, LOGGER};
            return Optional.ofNullable(accessStrategyScriptResource.execute(args,
                AuditableExecutionResult.class, true));
        }
        return Optional.empty();
    }
}
