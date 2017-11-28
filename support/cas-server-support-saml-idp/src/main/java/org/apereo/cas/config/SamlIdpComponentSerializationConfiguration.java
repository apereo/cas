package org.apereo.cas.config;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicy;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdpComponentSerializationConfiguration}.
 *
 * @author Bob Sandiford
 * @since 5.2.0
 */
@Configuration("samlIdpComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdpComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(SamlArtifactTicketExpirationPolicy.class);
        plan.registerSerializableClass(SamlAttributeQueryTicketExpirationPolicy.class);
    }

}
