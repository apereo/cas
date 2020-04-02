package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.EduPersonTargetedIdAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.GroovySamlRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.MetadataEntityAttributesAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.MetadataRequestedAttributesAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicy;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicy;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdpComponentSerializationConfiguration}.
 *
 * @author Bob Sandiford
 * @since 5.2.0
 */
@Configuration(value = "samlIdpComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdpComponentSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer samlIdPComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(SamlArtifactTicketExpirationPolicy.class);
            plan.registerSerializableClass(SamlAttributeQueryTicketExpirationPolicy.class);
            plan.registerSerializableClass(SamlRegisteredService.class);

            plan.registerSerializableClass(EduPersonTargetedIdAttributeReleasePolicy.class);
            plan.registerSerializableClass(GroovySamlRegisteredServiceAttributeReleasePolicy.class);
            plan.registerSerializableClass(MetadataEntityAttributesAttributeReleasePolicy.class);
            plan.registerSerializableClass(MetadataRequestedAttributesAttributeReleasePolicy.class);
            plan.registerSerializableClass(PatternMatchingEntityIdAttributeReleasePolicy.class);
        };
    }
}
