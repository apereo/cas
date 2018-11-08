package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.AuditableExecutionResult;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

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

        val builder = new ToStringBuilder(this, NO_CLASS_NAME_STYLE).append("result", outcome);
        surrogateEligibilityResult.getService().ifPresent(it -> builder.append("service", it.getId()));
        surrogateEligibilityResult.getAuthentication().ifPresent(it -> builder.append("selfPrincipal", it.getPrincipal()));
        builder.append("surrogatePrincipal", surrogateEligibilityResult.getProperties().get("targetUserId"));

        return new String[]{builder.toString()};
    }
}



