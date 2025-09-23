package org.apereo.cas.services;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class RegisteredServiceAccessStrategyAuditableEnforcer extends BaseAuditableExecution {

    private final ConfigurableApplicationContext applicationContext;
    private final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    private Optional<AuditableExecutionResult> byServiceTicketAndAuthnResultAndRegisteredService(final AuditableContext context) {
        val providedRegisteredService = context.getRegisteredService();
        if (context.getServiceTicket().isPresent() && context.getAuthenticationResult().isPresent()
            && providedRegisteredService.isPresent()) {
            val result = AuditableExecutionResult.of(context);
            try {
                val serviceTicket = context.getServiceTicket().orElseThrow();
                val authResult = context.getAuthenticationResult().orElseThrow().getAuthentication();
                ensurePrincipalAccessIsAllowedForService(providedRegisteredService.get(), serviceTicket.getService(), authResult);
            } catch (final Throwable e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Optional<AuditableExecutionResult> byServiceAndRegisteredServiceAndTicketGrantingTicket(final AuditableContext context) {
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
                val authResult = ticketGrantingTicket.get().getRoot().getAuthentication();
                ensurePrincipalAccessIsAllowedForService(registeredService, service, authResult);
            } catch (final Throwable e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    protected void ensurePrincipalAccessIsAllowedForService(final RegisteredService registeredService,
                                                            final Service service,
                                                            final Authentication authentication) throws Throwable {
        val attributes = CollectionUtils.merge(authentication.getAttributes(), authentication.getPrincipal().getAttributes());
        principalAccessStrategyEnforcer.authorize(
            RegisteredServicePrincipalAccessStrategyEnforcer.PrincipalAccessStrategyContext.builder()
                .registeredService(registeredService)
                .principalId(authentication.getPrincipal().getId())
                .principalAttributes(attributes)
                .service(service)
                .applicationContext(applicationContext)
                .build());
    }

    private static Optional<AuditableExecutionResult> byRegisteredService(final AuditableContext context) {
        val providedRegisteredService = context.getRegisteredService();
        if (providedRegisteredService.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(context.getService().orElse(null))
                .authentication(context.getAuthentication().orElse(null))
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

    /**
     * By service and registered service and principal optional.
     *
     * @param context the context
     * @return the optional
     */
    private Optional<AuditableExecutionResult> byServiceAndRegisteredServiceAndPrincipal(final AuditableContext context) {
        val providedService = context.getService();
        val providedRegisteredService = context.getRegisteredService();
        val providedPrincipal = context.getPrincipal();
        if (providedService.isPresent() && providedRegisteredService.isPresent() && providedPrincipal.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();
            val principal = providedPrincipal.get();

            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .build();

            try {
                principalAccessStrategyEnforcer.authorize(
                    RegisteredServicePrincipalAccessStrategyEnforcer.PrincipalAccessStrategyContext.builder()
                        .registeredService(registeredService)
                        .principalId(principal.getId())
                        .principalAttributes(principal.getAttributes())
                        .service(service)
                        .applicationContext(applicationContext)
                        .build());
            } catch (final Throwable e) {
                result.setException(e);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Optional<AuditableExecutionResult> byServiceAndRegisteredServiceAndAuthentication(final AuditableContext context) {
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
                ensurePrincipalAccessIsAllowedForService(registeredService, service, authentication);
            } catch (final Throwable e) {
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
        return byExternalAccessStrategyEnforcers(context)
            .or(() -> byServiceTicketAndAuthnResultAndRegisteredService(context))
            .or(() -> byServiceAndRegisteredServiceAndTicketGrantingTicket(context))
            .or(() -> byServiceAndRegisteredServiceAndPrincipal(context))
            .or(() -> byServiceAndRegisteredServiceAndAuthentication(context))
            .or(() -> byServiceAndRegisteredService(context))
            .or(() -> byRegisteredService(context))
            .orElseGet(() -> {
                val result = AuditableExecutionResult.builder()
                    .registeredService(context.getRegisteredService().orElse(null))
                    .service(context.getService().orElse(null))
                    .authentication(context.getAuthentication().orElse(null))
                    .build();
                result.setException(UnauthorizedServiceException.denied("Unauthorized"));
                LOGGER.warn("Service is not registered in the service registry. "
                        + "Service is [{}] and registered service is [{}]",
                    result.getService().orElse(null),
                    result.getRegisteredService().orElse(null));
                return result;
            });
    }

    protected Optional<AuditableExecutionResult> byExternalAccessStrategyEnforcers(final AuditableContext context) {
        val enforcers = applicationContext.getBeansOfType(RegisteredServiceAccessStrategyEnforcer.class).values();
        return enforcers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .map(Unchecked.function(enforcer -> enforcer.execute(context)))
            .filter(Objects::nonNull)
            .filter(AuditableExecutionResult::isExecutionFailure)
            .findFirst();
    }

}
