package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.AuditableExecutionResult;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;
import java.util.Objects;


/**
 * Inspektr's resource resolver for audit advice weaved at various surrogate authentication eligibility verification audit execution
 * joinpoints.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class SurrogateEligibilityVerificationAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        Objects.requireNonNull(returnValue, "AuditableExecutionResult must not be null");
        val surrogateEligibilityResult = AuditableExecutionResult.class.cast(returnValue);
        val outcome = "Surrogate Authentication " + BooleanUtils
            .toString(surrogateEligibilityResult.getProperties().containsKey("eligible"), "Eligible", "Ineligible");
        val values = new HashMap<>();
        values.put("result", outcome);
        surrogateEligibilityResult.getService().ifPresent(it -> values.put("service", it.getId()));
        surrogateEligibilityResult.getAuthentication().ifPresent(it -> values.put("selfPrincipal", it.getPrincipal()));
        values.put("surrogatePrincipal", surrogateEligibilityResult.getProperties().get("targetUserId"));
        return new String[]{auditFormat.serialize(values)};
    }
}



