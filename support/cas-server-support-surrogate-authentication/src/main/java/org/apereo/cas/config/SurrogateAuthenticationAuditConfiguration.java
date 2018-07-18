package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.audit.SurrogateAuthenticationEligibilityAuditableExecution;
import org.apereo.cas.authentication.audit.SurrogateEligibilityVerificationAuditResourceResolver;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateAuthenticationAuditConfiguration}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration("surrogateAuthenticationAuditConfiguration")
public class SurrogateAuthenticationAuditConfiguration implements AuditTrailRecordResolutionPlanConfigurer {

    @Bean
    public SurrogateAuthenticationEligibilityAuditableExecution surrogateEligibilityAuditableExecution() {
        return new SurrogateAuthenticationEligibilityAuditableExecution();
    }

    @Bean
    public SurrogateEligibilityVerificationAuditResourceResolver surrogateEligibilityVerificationAuditResourceResolver() {
        return new SurrogateEligibilityVerificationAuditResourceResolver();
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        val actionResolver = new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY);
        plan.registerAuditActionResolver("SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_ACTION_RESOLVER", actionResolver);

        plan.registerAuditResourceResolver("SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_RESOURCE_RESOLVER",
            surrogateEligibilityVerificationAuditResourceResolver());
    }
}
