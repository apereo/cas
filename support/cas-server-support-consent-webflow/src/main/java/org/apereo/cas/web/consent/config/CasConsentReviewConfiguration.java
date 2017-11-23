package org.apereo.cas.web.consent.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.web.pac4j.CasSecurityInterceptor;
import org.apereo.cas.web.consent.CasConsentReviewController;
import org.pac4j.cas.authorization.DefaultCasAuthorizationGenerator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.direct.DirectCasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.config.Config;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Map;
import javax.annotation.PostConstruct;

/**
 * This is {@link CasConsentReviewConfiguration}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Configuration("casConsentReviewConfiguration")
@ConditionalOnBean(name = "casSecurityContextConfiguration")
public class CasConsentReviewConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConsentReviewConfiguration.class);
    
    private static final String CAS_CONSENT_CLIENT = "CasConsentClient";
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAdminPagesPac4jConfig")
    private Config casAdminPagesPac4jConfig;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

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
        final CasConfiguration conf = new CasConfiguration(casProperties.getServer().getLoginUrl());
        
        final CasClient client = new CasClient(conf);
        client.setName(CAS_CONSENT_CLIENT);
        client.setCallbackUrl(casProperties.getServer().getPrefix().concat("/consentReview/callback"));
        client.setAuthorizationGenerator(new DefaultCasAuthorizationGenerator<>());
        client.setIncludeClientNameInCallbackUrl(false);
        
        final Clients clients = new Clients(client);
        clients.setDefaultClient(client);
        
        final Config config = new Config(clients);
        config.setAuthorizer(new IsAuthenticatedAuthorizer());
        config.setCallbackLogic(new DefaultCallbackLogic());
        config.setLogoutLogic(new DefaultLogoutLogic());
        
        // get role authorizer from admin pages for smooth integration
        final Map<String, Authorizer> adminAuthorizers = casAdminPagesPac4jConfig.getAuthorizers();
        final String auth = RequireAnyRoleAuthorizer.class.getSimpleName();
        if (adminAuthorizers.containsKey(auth)) {
            config.addAuthorizer(auth, adminAuthorizers.get(auth));
            final BaseClient adminClient = (BaseClient) casAdminPagesPac4jConfig.getClients().findClient(DirectCasClient.class);
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
    
    /**
    * Initialize consent service.
    */
    @PostConstruct
    protected void registerConsentService() {
        final Service callbackService = this.webApplicationServiceFactory.createService(
                casProperties.getServer().getPrefix().concat("/consentReview/callback"));
        if (!this.servicesManager.matchesExistingService(callbackService)) {
            LOGGER.debug("Initializing consent service [{}]", callbackService);

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setEvaluationOrder(0);
            service.setName("CAS Consent Review");
            service.setDescription("Review consent decisions for attribute release");
            service.setServiceId(callbackService.getId());
            
            // disable consent for this service
            final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
            final DefaultRegisteredServiceConsentPolicy consentPolicy = new DefaultRegisteredServiceConsentPolicy();
            consentPolicy.setEnabled(false);
            policy.setConsentPolicy(consentPolicy);
            service.setAttributeReleasePolicy(policy);
            
            LOGGER.debug("Saving consent service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
        }
    }
    
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(casConsentReviewSecurityInterceptor())
                .addPathPatterns("/consentReview", "/consentReview/*")
                .excludePathPatterns("/consentReview/logout*", "/consentReview/callback*");
    }
}
