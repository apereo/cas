package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilderConfigurer;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.slo.OidcSingleLogoutMessageCreator;
import org.apereo.cas.oidc.slo.OidcSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.oidc.slo.OidcSingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.UrlValidator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@Configuration(value = "OidcLogoutConfiguration", proxyBeanMethods = false)
class OidcLogoutConfiguration {

    @Configuration(value = "OidcLogoutBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcLogoutBuilderConfiguration {

        @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceLogoutUrlBuilderConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutServiceLogoutUrlBuilderConfigurer oidcSingleLogoutServiceLogoutUrlBuilderConfigurer(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator) {
            return () -> new OidcSingleLogoutServiceLogoutUrlBuilder(servicesManager, urlValidator);
        }
    }

    @Configuration(value = "OidcLogoutMessageConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcLogoutMessageConfiguration {

        @ConditionalOnMissingBean(name = "oidcSingleLogoutMessageCreator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutMessageCreator oidcSingleLogoutMessageCreator(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcSingleLogoutMessageCreator(oidcConfigurationContext);
        }
    }

    @Configuration(value = "OidcLogoutHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcLogoutHandlerConfiguration {

        @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceMessageHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("oidcSingleLogoutMessageCreator")
            final SingleLogoutMessageCreator oidcSingleLogoutMessageCreator,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("singleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT_NO_REDIRECT)
            final HttpClient httpClient,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService) {
            return new OidcSingleLogoutServiceMessageHandler(httpClient,
                oidcSingleLogoutMessageCreator,
                servicesManager,
                singleLogoutServiceLogoutUrlBuilder,
                casProperties.getSlo().isAsynchronous(),
                authenticationServiceSelectionPlan,
                oidcIssuerService);
        }

    }

    @Configuration(value = "OidcLogoutExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcLogoutExecutionPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oidcLogoutExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LogoutExecutionPlanConfigurer oidcLogoutExecutionPlanConfigurer(
            @Qualifier("oidcSingleLogoutServiceMessageHandler")
            final SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler) {
            return plan -> plan.registerSingleLogoutServiceMessageHandler(oidcSingleLogoutServiceMessageHandler);
        }
    }
}
