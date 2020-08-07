package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.audit.SurrogateAuthenticationEligibilityAuditableExecution;
import org.apereo.cas.authentication.audit.SurrogateEligibilitySelectionAuditResourceResolver;
import org.apereo.cas.authentication.audit.SurrogateEligibilityVerificationAuditResourceResolver;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateAuthenticationAuditConfiguration}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration("surrogateAuthenticationAuditConfiguration")
public class SurrogateAuthenticationAuditConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "surrogateEligibilityAuditableExecution")
    public AuditableExecution surrogateEligibilityAuditableExecution() {
        return new SurrogateAuthenticationEligibilityAuditableExecution();
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateEligibilityVerificationAuditResourceResolver")
    public AuditResourceResolver surrogateEligibilityVerificationAuditResourceResolver() {
        return new SurrogateEligibilityVerificationAuditResourceResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateEligibilitySelectionAuditResourceResolver")
    public AuditResourceResolver surrogateEligibilitySelectionAuditResourceResolver() {
        return new SurrogateEligibilitySelectionAuditResourceResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer surrogateAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            val actionResolver = new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY);
            plan.registerAuditActionResolver("SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_ACTION_RESOLVER", actionResolver);
            plan.registerAuditActionResolver("SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_ACTION_RESOLVER", actionResolver);

            plan.registerAuditResourceResolver("SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_RESOURCE_RESOLVER",
                surrogateEligibilityVerificationAuditResourceResolver());
            plan.registerAuditResourceResolver("SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_RESOURCE_RESOLVER",
                surrogateEligibilitySelectionAuditResourceResolver());
        };
    }
}
