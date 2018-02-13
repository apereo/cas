package org.apereo.cas.audit.spi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer.ServiceAccessCheckResult;
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
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        Objects.requireNonNull(retval, "ServiceAccessCheckResult must not be null");
        final ServiceAccessCheckResult serviceAccessCheckResult = ServiceAccessCheckResult.class.cast(retval);
        final String accessCheckOutcome = serviceAccessCheckResult.accessDenied() ? "ACCESS DENIED" : "ACCESS GRANTED";

        final String result = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                .append("service_access_check_outcome", accessCheckOutcome)
                .append("service", serviceAccessCheckResult.getServiceId())
                .append("principal_attributes", serviceAccessCheckResult.getPrincipalAttributes())
                .append("required_attributes_defined_by_policy", serviceAccessCheckResult.getServiceAccessRequiredAttributes())
                .toString();

        return new String[]{result};
    }
}
