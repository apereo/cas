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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OidcLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "oidcLogoutConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcLogoutConfiguration {

    @ConditionalOnMissingBean(name = "oidcSingleLogoutMessageCreator")
    @Bean
    @RefreshScope
    @Autowired
    public SingleLogoutMessageCreator oidcSingleLogoutMessageCreator(
        @Qualifier("oidcConfigurationContext")
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcSingleLogoutMessageCreator(oidcConfigurationContext);
    }

    @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceLogoutUrlBuilderConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public SingleLogoutServiceLogoutUrlBuilderConfigurer oidcSingleLogoutServiceLogoutUrlBuilderConfigurer(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("urlValidator")
        final UrlValidator urlValidator) {
        return () -> new OidcSingleLogoutServiceLogoutUrlBuilder(servicesManager, urlValidator);
    }

    @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceMessageHandler")
    @Bean
    @RefreshScope
    @Autowired
    public SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler(
        final CasConfigurationProperties casProperties,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("oidcSingleLogoutMessageCreator")
        final SingleLogoutMessageCreator oidcSingleLogoutMessageCreator,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
        @Qualifier("singleLogoutServiceLogoutUrlBuilder")
        final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
        @Qualifier("noRedirectHttpClient")
        final HttpClient httpClient,
        @Qualifier("oidcIssuerService")
        final OidcIssuerService oidcIssuerService) {
        return new OidcSingleLogoutServiceMessageHandler(httpClient,
            oidcSingleLogoutMessageCreator,
            servicesManager,
            singleLogoutServiceLogoutUrlBuilder,
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan,
            oidcIssuerService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcLogoutExecutionPlanConfigurer")
    @Autowired
    public LogoutExecutionPlanConfigurer oidcLogoutExecutionPlanConfigurer(
        @Qualifier("oidcSingleLogoutServiceMessageHandler")
        final SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler) {
        return plan -> plan.registerSingleLogoutServiceMessageHandler(oidcSingleLogoutServiceMessageHandler);
    }
}
