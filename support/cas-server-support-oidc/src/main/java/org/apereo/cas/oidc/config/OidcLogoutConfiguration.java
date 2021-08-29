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

/**
 * This is {@link OidcLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcLogoutConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcLogoutConfiguration {
    @Autowired
    @Qualifier("urlValidator")
    private ObjectProvider<UrlValidator> urlValidator;

    @Autowired
    @Qualifier("singleLogoutServiceLogoutUrlBuilder")
    private ObjectProvider<SingleLogoutServiceLogoutUrlBuilder> singleLogoutServiceLogoutUrlBuilder;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("oidcIssuerService")
    private ObjectProvider<OidcIssuerService> oidcIssuerService;

    @Autowired
    @Qualifier("oidcConfigurationContext")
    private ObjectProvider<OidcConfigurationContext> oidcConfigurationContext;

    @ConditionalOnMissingBean(name = "oidcSingleLogoutMessageCreator")
    @Bean
    @RefreshScope
    public SingleLogoutMessageCreator oidcSingleLogoutMessageCreator() {
        return new OidcSingleLogoutMessageCreator(oidcConfigurationContext.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceLogoutUrlBuilderConfigurer")
    @Bean
    @RefreshScope
    public SingleLogoutServiceLogoutUrlBuilderConfigurer oidcSingleLogoutServiceLogoutUrlBuilderConfigurer() {
        return () -> new OidcSingleLogoutServiceLogoutUrlBuilder(servicesManager.getObject(), urlValidator.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceMessageHandler")
    @Bean
    @RefreshScope
    public SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler() {
        return new OidcSingleLogoutServiceMessageHandler(httpClient.getObject(),
            oidcSingleLogoutMessageCreator(),
            servicesManager.getObject(),
            singleLogoutServiceLogoutUrlBuilder.getObject(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getObject(),
            oidcIssuerService.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcLogoutExecutionPlanConfigurer")
    public LogoutExecutionPlanConfigurer oidcLogoutExecutionPlanConfigurer() {
        return plan -> plan.registerSingleLogoutServiceMessageHandler(oidcSingleLogoutServiceMessageHandler());
    }
}
