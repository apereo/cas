package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.serialization.DefaultTicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.DefaultTicketStringSerializationManager;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;

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

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(name = "ticketSerializationExecutionPlan")
    public TicketSerializationExecutionPlan ticketSerializationExecutionPlan() {
        val resolvers = applicationContext.getBeansOfType(TicketSerializationExecutionPlanConfigurer.class, false, true);
        val providers = new ArrayList<TicketSerializationExecutionPlanConfigurer>(resolvers.values());
        AnnotationAwareOrderComparator.sort(providers);
        val plan = new DefaultTicketSerializationExecutionPlan();
        providers.forEach(provider -> provider.configureTicketSerialization(plan));
        return plan;
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketSerializationManager")
    public TicketSerializationManager ticketSerializationManager() {
        return new DefaultTicketStringSerializationManager(ticketSerializationExecutionPlan());
    }

}
