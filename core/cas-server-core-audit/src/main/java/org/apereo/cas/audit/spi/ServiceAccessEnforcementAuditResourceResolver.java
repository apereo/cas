package org.apereo.cas.audit.spi;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/**
 * Inspektr's resource resolver for audit advice weaved at
 * <code>org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer#enforceServiceAccessStrategy</code> joinpoint.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class ServiceAccessEnforcementAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        Objects.requireNonNull(returnValue, "Audit execution result must not be null");
        final AuditableExecutionResult serviceAccessCheckResult = AuditableExecutionResult.class.cast(returnValue);
        final String accessCheckOutcome = "Service Access "
            + BooleanUtils.toString(serviceAccessCheckResult.isExecutionFailure(), "Denied", "Granted");

        final String result = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
            .append("result", accessCheckOutcome)
            .append("service", serviceAccessCheckResult.getService().getId())
            .append("principal", serviceAccessCheckResult.getAuthentication().getPrincipal())
            .append("requiredAttributes", serviceAccessCheckResult.getRegisteredService().getAccessStrategy().getRequiredAttributes())
            .toString();

        return new String[]{result};
    }
}
