package org.apereo.cas.oidc.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.apereo.cas.web.UrlValidator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration(value = "oidcLogoutConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcLogoutConfiguration {

    @Configuration(value = "OidcLogoutBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcLogoutBuilderConfiguration {

        @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceLogoutUrlBuilderConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SingleLogoutServiceLogoutUrlBuilderConfigurer oidcSingleLogoutServiceLogoutUrlBuilderConfigurer(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator) {
            return () -> new OidcSingleLogoutServiceLogoutUrlBuilder(servicesManager, urlValidator);
        }

    }

    @Configuration(value = "OidcLogoutMessageConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcLogoutMessageConfiguration {

        @ConditionalOnMissingBean(name = "oidcSingleLogoutMessageCreator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SingleLogoutMessageCreator oidcSingleLogoutMessageCreator(
            @Qualifier("oidcConfigurationContext")
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcSingleLogoutMessageCreator(oidcConfigurationContext);
        }
    }

    @Configuration(value = "OidcLogoutHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcLogoutHandlerConfiguration {


        @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceMessageHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
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
            @Qualifier("noRedirectHttpClient")
            final HttpClient httpClient,
            @Qualifier("oidcIssuerService")
            final OidcIssuerService oidcIssuerService) {
            return new OidcSingleLogoutServiceMessageHandler(httpClient,
                null,
                servicesManager,
                singleLogoutServiceLogoutUrlBuilder,
                casProperties.getSlo().isAsynchronous(),
                authenticationServiceSelectionPlan,
                oidcIssuerService);
        }

    }

    @Configuration(value = "OidcLogoutExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcLogoutExecutionPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oidcLogoutExecutionPlanConfigurer")
        @Autowired
        public LogoutExecutionPlanConfigurer oidcLogoutExecutionPlanConfigurer(
            @Qualifier("oidcSingleLogoutServiceMessageHandler")
            final SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler) {
            return plan -> plan.registerSingleLogoutServiceMessageHandler(oidcSingleLogoutServiceMessageHandler);
        }
    }
}
