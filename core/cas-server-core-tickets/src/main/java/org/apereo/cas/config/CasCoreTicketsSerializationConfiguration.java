package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.serialization.DefaultTicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.DefaultTicketStringSerializationManager;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreTicketsSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreTicketsSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreTicketsConfiguration.class)
public class CasCoreTicketsSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "ticketSerializationExecutionPlan")
    public TicketSerializationExecutionPlan ticketSerializationExecutionPlan() {
        return new DefaultTicketSerializationExecutionPlan();
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketSerializationManager")
    public TicketSerializationManager ticketSerializationManager() {
        return new DefaultTicketStringSerializationManager(ticketSerializationExecutionPlan());
    }

}
