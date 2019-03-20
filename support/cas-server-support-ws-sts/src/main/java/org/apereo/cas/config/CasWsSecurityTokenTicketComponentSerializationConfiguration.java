package org.apereo.cas.config;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultSecurityTokenTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasWsSecurityTokenTicketComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casWsSecurityTokenTicketComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWsSecurityTokenTicketComponentSerializationConfiguration
    implements ComponentSerializationPlanConfigurator, TicketSerializationExecutionPlanConfigurer {

    @Override
    public void configureTicketSerialization(final TicketSerializationExecutionPlan plan) {
        plan.registerTicketSerializer(new AbstractJacksonBackedStringSerializer<DefaultSecurityTokenTicket>() {
            private static final long serialVersionUID = -3198623586274810263L;

            @Override
            public Class<DefaultSecurityTokenTicket> getTypeToSerialize() {
                return DefaultSecurityTokenTicket.class;
            }
        });
    }

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(DefaultSecurityTokenTicket.class);
    }
}
