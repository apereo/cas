package org.apereo.cas.config;

import org.apereo.cas.adaptors.rest.RestAuthenticationHandler;
import org.apereo.cas.adaptors.rest.TenantRestAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasRestAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "rest")
@AutoConfiguration
public class CasRestAuthenticationAutoConfiguration {

    @ConditionalOnMissingBean(name = "restAuthenticationPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory restAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @ConditionalOnMissingBean(name = "restAuthenticationHandler")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<AuthenticationHandler> restAuthenticationHandler(
        @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
        final HttpClient httpClient,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("restAuthenticationPrincipalFactory")
        final PrincipalFactory restAuthenticationPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val rest = casProperties.getAuthn().getRest();
        val handlers = rest
            .stream()
            .map(prop -> new RestAuthenticationHandler(servicesManager,
                restAuthenticationPrincipalFactory, prop, applicationContext, httpClient))
            .toList();
        return BeanContainer.of(handlers);
    }

    @ConditionalOnMissingBean(name = "casRestAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer casRestAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("restAuthenticationHandler")
        final BeanContainer<AuthenticationHandler> restAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlersWithPrincipalResolver(restAuthenticationHandler.toList(), defaultPrincipalResolver);
    }


    @Configuration(value = "RestMultitenancyAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    static class RestMultitenancyAuthenticationConfiguration {
        @ConditionalOnMissingBean(name = "restMultitenancyAuthenticationPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer restMultitenancyAuthenticationPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("restAuthenticationPrincipalFactory")
            final PrincipalFactory restAuthenticationPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return plan -> {
                if (casProperties.getMultitenancy().getCore().isEnabled()) {
                    val builder = new TenantRestAuthenticationHandlerBuilder(applicationContext,
                        restAuthenticationPrincipalFactory, servicesManager, httpClient);
                    plan.registerTenantAuthenticationHandlerBuilder(builder);
                }
            };
        }
    }
}
