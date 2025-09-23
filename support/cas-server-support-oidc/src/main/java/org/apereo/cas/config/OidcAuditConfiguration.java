package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.audit.OidcCibaResponseAuditResourceResolver;
import org.apereo.cas.oidc.audit.OidcIdTokenAuditResourceResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
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
 * This is {@link OidcAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect, module = "audit")
@Configuration(value = "OidcAuditConfiguration", proxyBeanMethods = false)
class OidcAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "oidcIdTokenResourceResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditResourceResolver oidcIdTokenResourceResolver(final CasConfigurationProperties casProperties) {
        return new OidcIdTokenAuditResourceResolver(casProperties.getAudit().getEngine());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcCibaResponseResourceResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditResourceResolver oidcCibaResponseResourceResolver(final CasConfigurationProperties casProperties) {
        return new OidcCibaResponseAuditResourceResolver(casProperties.getAudit().getEngine());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcAuditTrailRecordResolutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailRecordResolutionPlanConfigurer oidcAuditTrailRecordResolutionPlanConfigurer(
        @Qualifier("oidcIdTokenResourceResolver")
        final AuditResourceResolver oidcIdTokenResourceResolver,
        @Qualifier("oidcCibaResponseResourceResolver")
        final AuditResourceResolver oidcCibaResponseResourceResolver,
        final CasConfigurationProperties casProperties) {
        return plan -> {
            plan.registerAuditActionResolver(AuditActionResolvers.OIDC_ID_TOKEN_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
            plan.registerAuditResourceResolver(AuditResourceResolvers.OIDC_ID_TOKEN_RESOURCE_RESOLVER, oidcIdTokenResourceResolver);


            plan.registerAuditActionResolver(AuditActionResolvers.OIDC_CIBA_RESPONSE_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
            plan.registerAuditResourceResolver(AuditResourceResolvers.OIDC_CIBA_RESPONSE_RESOURCE_RESOLVER, oidcCibaResponseResourceResolver);
            
        };
    }
}
