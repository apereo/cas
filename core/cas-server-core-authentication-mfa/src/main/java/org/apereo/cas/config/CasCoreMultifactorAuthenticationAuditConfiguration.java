package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.bypass.audit.MultifactorAuthenticationProviderBypassAuditResourceResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreMultifactorAuthenticationAuditConfiguration}.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeaturesEnabled({
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit),
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication)
})
@Configuration(value = "CasCoreMultifactorAuthenticationAuditConfiguration", proxyBeanMethods = false)
class CasCoreMultifactorAuthenticationAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casCoreMfaAuditTrailRecordResolutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailRecordResolutionPlanConfigurer casCoreMfaAuditTrailRecordResolutionPlanConfigurer(
        @Qualifier("casCoreMfaProviderBypassAuditActionResolver") final AuditActionResolver actionResourceResolver,
        @Qualifier("casCoreMfaProviderBypassAuditResourceResolver") final AuditResourceResolver auditResourceResolver) {
        return plan -> {
            plan.registerAuditResourceResolver(AuditResourceResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_RESOURCE_RESOLVER, auditResourceResolver);
            plan.registerAuditActionResolver(AuditActionResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_ACTION_RESOLVER, actionResourceResolver);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "casCoreMfaProviderBypassAuditActionResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditActionResolver casCoreMfaProviderBypassAuditActionResolver() {
        return new DefaultAuditActionResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "casCoreMfaProviderBypassAuditResourceResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditResourceResolver casCoreMfaProviderBypassAuditResourceResolver() {
        return new MultifactorAuthenticationProviderBypassAuditResourceResolver();
    }
}
