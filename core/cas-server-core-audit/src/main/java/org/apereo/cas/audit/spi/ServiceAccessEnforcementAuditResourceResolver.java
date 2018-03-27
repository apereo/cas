package org.apereo.cas.audit.spi;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/**
 * Inspektr's resource resolver for audit advice weaved at various service access enforcement audit execution joinpoints.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class ServiceAccessEnforcementAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        Objects.requireNonNull(returnValue, "AuditableExecutionResult must not be null");
        final AuditableExecutionResult serviceAccessCheckResult = AuditableExecutionResult.class.cast(returnValue);
        final String accessCheckOutcome = "Service Access "
            + BooleanUtils.toString(serviceAccessCheckResult.isExecutionFailure(), "Denied", "Granted");

        final ToStringBuilder builder = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
            .append("result", accessCheckOutcome);
        serviceAccessCheckResult.getService().ifPresent(service -> builder.append("service", service.getId()));
        serviceAccessCheckResult.getAuthentication().ifPresent(authn -> builder.append("principal", authn.getPrincipal()));
        serviceAccessCheckResult.getRegisteredService().ifPresent(regSvc ->
            builder.append("requiredAttributes", regSvc.getAccessStrategy().getRequiredAttributes()));

        return new String[]{builder.toString()};
    }
}
