package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.bypass.audit.MultifactorAuthenticationProviderBypassAuditResourceResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreMultifactorAuthenticationAuditConfiguration}.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Configuration(value = "casCoreMultifactorAuthenticationAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreMultifactorAuthenticationAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casCoreMfaAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer casCoreMfaAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditResourceResolver(AuditResourceResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_RESOURCE_RESOLVER,
                new MultifactorAuthenticationProviderBypassAuditResourceResolver());
            plan.registerAuditActionResolver(AuditActionResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_ACTION_RESOLVER,
                new DefaultAuditActionResolver());
        };
    }
}
