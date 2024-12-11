package org.apereo.inspektr.audit;

import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.audit.annotation.Audits;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.common.spi.AuditActionDateProvider;
import org.apereo.inspektr.common.spi.ClientInfoResolver;
import org.apereo.inspektr.common.spi.DefaultClientInfoResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.util.LoggingUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A POJO style aspect modularizing management of an audit trail data concern.
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @since 1.0
 */
@Aspect
@Slf4j
@Setter
@RequiredArgsConstructor
public class AuditTrailManagementAspect {

    private final PrincipalResolver defaultAuditPrincipalResolver;
    private final List<AuditTrailManager> auditTrailManagers;
    private final Map<String, AuditActionResolver> auditActionResolvers;
    private final Map<String, AuditResourceResolver> auditResourceResolvers;
    private final Map<String, PrincipalResolver> auditPrincipalResolvers;
    private final AuditTrailManager.AuditFormats auditFormat;
    private final AuditActionDateProvider auditActionDateProvider;

    private ClientInfoResolver clientInfoResolver = new DefaultClientInfoResolver();
    private boolean failOnAuditFailures = true;
    private boolean enabled = true;

    /**
     * Handle audit trail.
     *
     * @param joinPoint the join point
     * @param audits    the audits
     * @return the object
     * @throws Throwable the throwable
     */
    @Around(value = "@annotation(audits)", argNames = "audits")
    public Object handleAuditTrail(final ProceedingJoinPoint joinPoint, final Audits audits) throws Throwable {
        if (!this.enabled) {
            return joinPoint.proceed();
        }

        Object retVal = null;
        String currentPrincipal = null;
        val actions = new String[audits.value().length];
        val auditableResources = new String[audits.value().length][];
        try {
            retVal = joinPoint.proceed();

            currentPrincipal = getCurrentPrincipal(joinPoint, audits, retVal);

            for (var i = 0; i < audits.value().length; i++) {
                val auditActionResolver = auditActionResolvers.get(audits.value()[i].actionResolverName());

                val auditResourceResolver = auditResourceResolvers.get(audits.value()[i].resourceResolverName());
                auditResourceResolver.setAuditFormat(this.auditFormat);

                auditableResources[i] = auditResourceResolver.resolveFrom(joinPoint, retVal);
                actions[i] = auditActionResolver.resolveFrom(joinPoint, retVal, audits.value()[i]);
            }
            return retVal;
        } catch (final Throwable t) {
            val e = wrapIfNecessary(t);
            currentPrincipal = getCurrentPrincipal(joinPoint, audits, e);

            if (currentPrincipal != null) {
                for (var i = 0; i < audits.value().length; i++) {
                    var auditResourceResolver = this.auditResourceResolvers.get(audits.value()[i].resourceResolverName());
                    auditResourceResolver.setAuditFormat(this.auditFormat);

                    auditableResources[i] = auditResourceResolver.resolveFrom(joinPoint, e);
                    actions[i] = auditActionResolvers.get(audits.value()[i].actionResolverName()).resolveFrom(joinPoint, e, audits.value()[i]);
                }
            }
            throw t;
        } finally {
            for (var i = 0; i < audits.value().length; i++) {
                executeAuditCode(currentPrincipal, auditableResources[i], joinPoint, retVal, actions[i]);
            }
        }
    }

    /**
     * Handle audit trail.
     *
     * @param joinPoint the join point
     * @param audit     the audit
     * @return the object
     * @throws Throwable the throwable
     */
    @Around(value = "@annotation(audit)", argNames = "audit")
    public Object handleAuditTrail(final ProceedingJoinPoint joinPoint, final Audit audit) throws Throwable {
        if (!this.enabled) {
            return joinPoint.proceed();
        }

        val auditActionResolver = auditActionResolvers.get(audit.actionResolverName());
        Objects.requireNonNull(auditActionResolver, () -> "AuditActionResolver is undefined for %s".formatted(audit.actionResolverName()));
        val auditResourceResolver = auditResourceResolvers.get(audit.resourceResolverName());
        Objects.requireNonNull(auditResourceResolver, () -> "AuditActionResolver is undefined for %s".formatted(audit.actionResolverName()));
        auditResourceResolver.setAuditFormat(this.auditFormat);

        String currentPrincipal = null;
        var auditResource = new String[]{null};
        String action = null;
        Object retVal = null;
        try {
            retVal = joinPoint.proceed();

            currentPrincipal = getCurrentPrincipal(joinPoint, audit, retVal);

            auditResource = auditResourceResolver.resolveFrom(joinPoint, retVal);
            action = auditActionResolver.resolveFrom(joinPoint, retVal, audit);

            return retVal;
        } catch (final Throwable t) {
            val e = wrapIfNecessary(t);
            currentPrincipal = getCurrentPrincipal(joinPoint, audit, e);
            auditResource = auditResourceResolver.resolveFrom(joinPoint, e);
            action = auditActionResolver.resolveFrom(joinPoint, e, audit);
            throw t;
        } finally {
            executeAuditCode(currentPrincipal, auditResource, joinPoint, retVal, action);
        }
    }

    private String getCurrentPrincipal(final ProceedingJoinPoint joinPoint, final Audits audits, final Object retVal) {
        String currentPrincipal = null;
        for (var i = 0; i < audits.value().length; i++) {
            var resolverName = audits.value()[i].principalResolverName();
            if (!resolverName.trim().isEmpty()) {
                val resolver = this.auditPrincipalResolvers.get(resolverName);
                currentPrincipal = resolver.resolveFrom(joinPoint, retVal);
            }
        }

        if (currentPrincipal == null) {
            currentPrincipal = this.defaultAuditPrincipalResolver.resolveFrom(joinPoint, retVal);
        }
        return currentPrincipal;
    }

    private String getCurrentPrincipal(final ProceedingJoinPoint joinPoint, final Audit audit, final Object retVal) {
        String currentPrincipal = null;
        var resolverName = audit.principalResolverName();
        if (!resolverName.trim().isEmpty()) {
            val resolver = auditPrincipalResolvers.get(resolverName);
            currentPrincipal = resolver.resolveFrom(joinPoint, retVal);
        }
        if (currentPrincipal == null) {
            currentPrincipal = defaultAuditPrincipalResolver.resolveFrom(joinPoint, retVal);
        }
        return currentPrincipal;
    }

    private void executeAuditCode(final String currentPrincipal, final String[] auditableResources,
                                  final ProceedingJoinPoint joinPoint, final Object retVal, final String action) {
        val clientInfo = clientInfoResolver.resolveFrom(joinPoint, retVal);
        val actionDate = auditActionDateProvider.get();
        val runtimeInfo = new AspectJAuditPointRuntimeInfo(joinPoint);

        Assert.notNull(currentPrincipal, "'principal' cannot be null.\n" + getDiagnosticInfo(runtimeInfo));
        Assert.notNull(action, "'actionPerformed' cannot be null.\n" + getDiagnosticInfo(runtimeInfo));
        Assert.notNull(actionDate, "'whenActionPerformed' cannot be null.\n" + getDiagnosticInfo(runtimeInfo));
        Assert.notNull(clientInfo.getClientIpAddress(), "'clientIpAddress' cannot be null.\n" + getDiagnosticInfo(runtimeInfo));
        Assert.notNull(clientInfo.getServerIpAddress(), "'serverIpAddress' cannot be null.\n" + getDiagnosticInfo(runtimeInfo));

        for (val auditableResource : auditableResources) {
            Assert.notNull(auditableResource, "'resourceOperatedUpon' cannot be null.\n" + getDiagnosticInfo(runtimeInfo));
            val auditContext = new AuditActionContext(currentPrincipal, auditableResource, action, "CAS", actionDate, clientInfo);

            try {
                for (val manager : auditTrailManagers) {
                    manager.setAuditFormat(this.auditFormat);
                    manager.record(auditContext);
                }
            } catch (final Throwable e) {
                if (this.failOnAuditFailures) {
                    throw e;
                }
                LOGGER.error("Failed to record audit context for [{}] and principal [{}]",
                    auditContext.getActionPerformed(), auditContext.getPrincipal());
                LoggingUtils.error(LOGGER, e);
            }
        }
    }

    private static String getDiagnosticInfo(final AuditPointRuntimeInfo runtimeInfo) {
        return "Check the correctness of @Audit annotation at the following audit point: " + runtimeInfo.toString();
    }

    private static Exception wrapIfNecessary(final Throwable throwable) {
        return throwable instanceof final Exception ex ? ex : new Exception(throwable);
    }
}
