package org.apereo.cas.web.consent.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.services.ConsentServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsentReviewConfiguration}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Configuration("casConsentReviewConfiguration")
@Slf4j
public class CasConsentReviewConfiguration implements ServiceRegistryExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Bean
    public Service consentCallbackService() {
        return this.webApplicationServiceFactory.createService(
            casProperties.getServer().getPrefix().concat("/consentReview/callback"));
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        final var service = new RegexRegisteredService();
        service.setEvaluationOrder(0);
        service.setName("CAS Consent Review");
        service.setDescription("Review consent decisions for attribute release");
        service.setServiceId(consentCallbackService().getId());
        final var policy = new ReturnAllowedAttributeReleasePolicy();
        final var consentPolicy = new DefaultRegisteredServiceConsentPolicy();
        consentPolicy.setEnabled(false);
        policy.setConsentPolicy(consentPolicy);
        service.setAttributeReleasePolicy(policy);

        LOGGER.debug("Saving consent service [{}] into the registry", service);
        plan.registerServiceRegistry(new ConsentServiceRegistry(service));
    }
}
