package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.syncope.SyncopeUtils;
import org.apereo.cas.syncope.TenantSyncopeAuthenticationHandlerBuilder;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SyncopeAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "syncope")
@Configuration(value = "SyncopeAuthenticationConfiguration", proxyBeanMethods = false)
class SyncopeAuthenticationConfiguration {
    @ConditionalOnMissingBean(name = "syncopePrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory syncopePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("syncopePrincipalFactory")
        final PrincipalFactory syncopePrincipalFactory,
        @Qualifier("syncopePasswordPolicyConfiguration")
        final PasswordPolicyContext syncopePasswordPolicyConfiguration,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val syncope = casProperties.getAuthn().getSyncope();
        val handlers = SyncopeUtils.newAuthenticationHandlers(syncope, applicationContext,
            syncopePrincipalFactory, servicesManager, syncopePasswordPolicyConfiguration);
        return BeanContainer.of(handlers);
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer syncopeAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("syncopeAuthenticationHandlers")
        final BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val syncope = casProperties.getAuthn().getSyncope();
            FunctionUtils.doIf(syncope.isDefined(),
                    o -> syncopeAuthenticationHandlers.toList().forEach(
                        handler -> plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver)))
                .accept(syncope);
        };
    }

    @ConditionalOnMissingBean(name = "syncopePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext syncopePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @Configuration(value = "SyncopeAuthenticationMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SyncopeAuthenticationMultitenancyConfiguration {
        @ConditionalOnMissingBean(name = "syncopeMultitenancyAuthenticationPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer syncopeMultitenancyAuthenticationPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("syncopePrincipalFactory")
            final PrincipalFactory syncopePrincipalFactory,
            @Qualifier("syncopePasswordPolicyConfiguration")
            final PasswordPolicyContext syncopePasswordPolicyConfiguration,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return plan -> {
                if (casProperties.getMultitenancy().getCore().isEnabled()) {
                    val builder = new TenantSyncopeAuthenticationHandlerBuilder(
                        syncopePasswordPolicyConfiguration, syncopePrincipalFactory,
                        applicationContext, servicesManager);
                    plan.registerTenantAuthenticationHandlerBuilder(builder);
                }
            };
        }
    }
}
