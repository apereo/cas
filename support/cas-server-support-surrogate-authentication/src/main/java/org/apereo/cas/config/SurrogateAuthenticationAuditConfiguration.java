package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.audit.SurrogateAuthenticationEligibilityAuditableExecution;
import org.apereo.cas.authentication.audit.SurrogateEligibilitySelectionAuditResourceResolver;
import org.apereo.cas.authentication.audit.SurrogateEligibilityVerificationAuditResourceResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateAuthenticationAuditConfiguration}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration(value = "surrogateAuthenticationAuditConfiguration", proxyBeanMethods = false)
public class SurrogateAuthenticationAuditConfiguration {

    @Configuration(value = "SurrogateAuthenticationAuditExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationAuditExecutionConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "surrogateEligibilityAuditableExecution")
        public AuditableExecution surrogateEligibilityAuditableExecution() {
            return new SurrogateAuthenticationEligibilityAuditableExecution();
        }
    }

    @Configuration(value = "SurrogateAuthenticationAuditResourcesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationAuditResourcesConfiguration {
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

    }

    @Configuration(value = "SurrogateAuthenticationAuditPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationAuditPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "surrogateAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer surrogateAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("surrogateEligibilityVerificationAuditResourceResolver")
            final AuditResourceResolver surrogateEligibilityVerificationAuditResourceResolver,
            @Qualifier("surrogateEligibilitySelectionAuditResourceResolver")
            final AuditResourceResolver surrogateEligibilitySelectionAuditResourceResolver) {
            return plan -> {
                val actionResolver = new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY);
                plan.registerAuditActionResolver(AuditActionResolvers.SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_ACTION_RESOLVER, actionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_ACTION_RESOLVER, actionResolver);

                plan.registerAuditResourceResolver(AuditResourceResolvers.SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION_RESOURCE_RESOLVER,
                    surrogateEligibilityVerificationAuditResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_RESOURCE_RESOLVER,
                    surrogateEligibilitySelectionAuditResourceResolver);
            };
        }
    }
}
