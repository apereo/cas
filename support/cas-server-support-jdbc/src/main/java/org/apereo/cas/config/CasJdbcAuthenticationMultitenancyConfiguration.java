package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.jdbc.TenantJdbcAuthenticationHandlerBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
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
 * This is {@link CasJdbcAuthenticationMultitenancyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "jdbc")
@Configuration(value = "CasJdbcMultitenancyConfiguration", proxyBeanMethods = false)
class CasJdbcAuthenticationMultitenancyConfiguration {

    @Configuration(value = "CasJdbcMultitenancyAuthenticationHandlersConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasJdbcMultitenancyAuthenticationHandlersConfiguration {
        @ConditionalOnMissingBean(name = "jdbcMultitenancyAuthenticationPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer jdbcMultitenancyAuthenticationPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return plan -> {
                val passwordPolicyConfiguration = new PasswordPolicyContext();
                val principalFactory = PrincipalFactoryUtils.newPrincipalFactory();
                val builder = new TenantJdbcAuthenticationHandlerBuilder(passwordPolicyConfiguration, principalFactory,
                    applicationContext, servicesManager);
                plan.registerTenantAuthenticationHandlerBuilder(builder);
            };
        }
    }
}
