package org.apereo.cas.audit.spi.plan;

import lombok.val;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.apereo.inspektr.audit.spi.support.SpringWebflowActionExecutionAuditablePrincipalResolver;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuditTrailRecordResolutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Audits")
public class DefaultAuditTrailRecordResolutionPlanTests {
    @Test
    public void verifyOperation() {
        val plan = new DefaultAuditTrailRecordResolutionPlan();
        plan.registerAuditActionResolver("action", new DefaultAuditActionResolver());
        plan.registerAuditResourceResolver("resource", new ReturnValueAsStringResourceResolver());
        plan.registerAuditPrincipalResolver("principal",
            new SpringWebflowActionExecutionAuditablePrincipalResolver("key"));

        assertFalse(plan.getAuditActionResolvers().isEmpty());
        assertFalse(plan.getAuditPrincipalResolvers().isEmpty());
        assertFalse(plan.getAuditActionResolvers().isEmpty());
    }
}
