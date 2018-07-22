package org.apereo.cas.web.consent.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.web.consent.CasConsentReviewController;
import org.apereo.cas.web.pac4j.CasSecurityInterceptor;

import lombok.val;
import org.pac4j.cas.authorization.DefaultCasAuthorizationGenerator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.direct.DirectCasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This is {@link CasConsentReviewWebConfiguration}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Configuration("casConsentReviewWebConfiguration")
public class CasConsentReviewWebConfiguration implements WebMvcConfigurer, ServiceRegistryExecutionPlanConfigurer {
    private static final String CAS_CONSENT_CLIENT = "CasConsentClient";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAdminPagesPac4jConfig")
    private Config casAdminPagesPac4jConfig;

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier("consentEngine")
    private ConsentEngine consentEngine;

    @Bean
    @RefreshScope
    public CasConsentReviewController casConsentReviewController() {
        return new CasConsentReviewController(consentRepository, consentEngine, casConsentPac4jConfig(), casProperties);
    }

    @Bean
    @RefreshScope
    public Config casConsentPac4jConfig() {
        val conf = new CasConfiguration(casProperties.getServer().getLoginUrl());

        val client = new CasClient(conf);
        client.setName(CAS_CONSENT_CLIENT);
        client.setCallbackUrl(casProperties.getServer().getPrefix().concat("/consentReview/callback"));
        client.setAuthorizationGenerator(new DefaultCasAuthorizationGenerator<>());

        val clients = new Clients(client);
        val config = new Config(clients);
        config.setAuthorizer(new IsAuthenticatedAuthorizer());
        config.setCallbackLogic(new DefaultCallbackLogic());
        config.setLogoutLogic(new DefaultLogoutLogic());

        // get role authorizer from admin pages for smooth integration
        val adminAuthorizers = casAdminPagesPac4jConfig.getAuthorizers();
        val auth = RequireAnyRoleAuthorizer.class.getSimpleName();
        if (adminAuthorizers.containsKey(auth)) {
            config.addAuthorizer(auth, adminAuthorizers.get(auth));
            val adminClient = casAdminPagesPac4jConfig.getClients().findClient(DirectCasClient.class);
            client.addAuthorizationGenerators(adminClient.getAuthorizationGenerators());
        }
        return config;
    }

    @Bean
    @RefreshScope
    public CasSecurityInterceptor casConsentReviewSecurityInterceptor() {
        return new CasSecurityInterceptor(casConsentPac4jConfig(), CAS_CONSENT_CLIENT,
            "securityHeaders,csrfToken,".concat(IsAuthenticatedAuthorizer.class.getSimpleName()));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(casConsentReviewSecurityInterceptor())
            .addPathPatterns("/consentReview", "/consentReview/*")
            .excludePathPatterns("/consentReview/logout*", "/consentReview/callback*");
    }
}
