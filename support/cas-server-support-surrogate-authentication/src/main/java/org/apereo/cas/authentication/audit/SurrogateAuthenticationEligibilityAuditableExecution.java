package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;

import org.apereo.inspektr.audit.annotation.Audit;

/**
 * This is an auditable execution to capture data at audit point pertaining to checking for surrogate authentication eligibility.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class SurrogateAuthenticationEligibilityAuditableExecution extends BaseAuditableExecution {

    @Override
    @Audit(action = "SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION",
        actionResolverName = "SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_ACTION_RESOLVER",
        resourceResolverName = "SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_RESOURCE_RESOLVER")
    public AuditableExecutionResult execute(final AuditableContext context) {
        return super.execute(context);
    }
}
