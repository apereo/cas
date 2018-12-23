package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.audit.AuditableExecutionResult;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
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

    private static final int ABBREV_LENGTH = 40;

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        Objects.requireNonNull(returnValue, "AuditableExecutionResult must not be null");
        val serviceAccessCheckResult = AuditableExecutionResult.class.cast(returnValue);
        val accessCheckOutcome = "Service Access "
            + BooleanUtils.toString(serviceAccessCheckResult.isExecutionFailure(), "Denied", "Granted");

        val builder = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
            .append("result", accessCheckOutcome);
        serviceAccessCheckResult.getService()
            .ifPresent(service -> builder.append("service", StringUtils.abbreviate(service.getId(), ABBREV_LENGTH)));
        serviceAccessCheckResult.getAuthentication()
            .ifPresent(authn -> builder.append("principal", authn.getPrincipal()));
        serviceAccessCheckResult.getRegisteredService()
            .ifPresent(regSvc -> builder.append("requiredAttributes", regSvc.getAccessStrategy().getRequiredAttributes()));

        return new String[]{builder.toString()};
    }
}
