package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;
import java.util.Objects;

/**
 * Inspektr's resource resolver for audit advice weaved at various service access enforcement audit execution joinpoints.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class ServiceAccessEnforcementAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    protected final AuthenticationServiceSelectionPlan serviceSelectionStrategy;
    protected final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        Objects.requireNonNull(returnValue, "AuditableExecutionResult must not be null");
        val serviceAccessCheckResult = (AuditableExecutionResult) returnValue;
        val accessCheckOutcome = "Service Access " + BooleanUtils.toString(serviceAccessCheckResult.isExecutionFailure(), "Denied", "Granted");
        val values = new HashMap<>();
        values.put("result", accessCheckOutcome);
        serviceAccessCheckResult.getService().ifPresent(service -> values.put("service", getServiceId(service)));
        serviceAccessCheckResult.getAuthentication()
            .ifPresent(authn -> values.put("principal", authn.getPrincipal()));
        serviceAccessCheckResult.getRegisteredService()
            .ifPresent(regSvc -> values.put("requiredAttributes", regSvc.getAccessStrategy().getRequiredAttributes()));
        return new String[]{auditFormat.serialize(values)};
    }

    private String getServiceId(final Service service) {
        val serviceId = FunctionUtils.doUnchecked(() -> serviceSelectionStrategy.resolveService(service).getId());
        return DigestUtils.abbreviate(serviceId, properties.getAbbreviationLength());
    }
}
