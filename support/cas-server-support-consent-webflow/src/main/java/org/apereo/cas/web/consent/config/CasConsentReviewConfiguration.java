package org.apereo.cas.web.consent.config;

import java.util.List;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.pac4j.CasSecurityInterceptor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.consent.CasConsentReviewController;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.config.Config;
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

import javax.annotation.PostConstruct;
import org.pac4j.core.client.Client;

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
    public CasConsentReviewController casConsentReviewController() {
        return new CasConsentReviewController(consentRepository, consentEngine);
    }

    @Bean
    @RefreshScope
    public Config casConsentPac4jConfig() {
        final List<Client> clients = casAdminPagesPac4jConfig.getClients().getClients();
        if (clients != null && !clients.isEmpty()) {
            final Config config = new Config(casProperties.getServer().getPrefix().concat("/consent"), clients);
            config.setAuthorizers(casAdminPagesPac4jConfig.getAuthorizers());
            final String auth = IsAuthenticatedAuthorizer.class.getSimpleName();
            if (!config.getAuthorizers().containsKey(auth)) {
                config.addAuthorizer(auth, new IsAuthenticatedAuthorizer());
            }
            return config;
        }
        return new Config();
    }

    @Bean
    @RefreshScope
    public CasSecurityInterceptor casConsentReviewSecurityInterceptor() {
        return new CasSecurityInterceptor(casConsentPac4jConfig(), "CasClient",
                "securityHeaders,csrfToken,".concat(IsAuthenticatedAuthorizer.class.getSimpleName()));
    }

    /**
    * Initialize consent service.
    */
    @PostConstruct
    protected void registerConsentService() {
        final Service callbackService = this.webApplicationServiceFactory.createService(
                casProperties.getServer().getPrefix().concat("/consent.*"));
        if (!this.servicesManager.matchesExistingService(callbackService)) {
            LOGGER.debug("Initializing consent service [{}]", callbackService);

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("CAS Consent Overview");
            service.setServiceId(callbackService.getId());

            LOGGER.debug("Saving consent service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
        }
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(casConsentReviewSecurityInterceptor()).addPathPatterns("/consent")
                .addPathPatterns("/consent/*");
    }
}
