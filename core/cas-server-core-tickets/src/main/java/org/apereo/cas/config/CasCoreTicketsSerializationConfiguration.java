package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.serialization.DefaultTicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.DefaultTicketStringSerializationManager;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasCoreTicketsSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreTicketsSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreTicketsSerializationConfiguration {

    @Configuration(value = "CasCoreTicketsSerializationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreTicketsSerializationPlanConfiguration {
        @Autowired
        @Bean
        @ConditionalOnMissingBean(name = "ticketSerializationExecutionPlan")
        public TicketSerializationExecutionPlan ticketSerializationExecutionPlan(
            final ObjectProvider<List<TicketSerializationExecutionPlanConfigurer>> providerList) {
            val providers = Optional.ofNullable(providerList.getIfAvailable()).orElse(new ArrayList<>());
            AnnotationAwareOrderComparator.sort(providers);
            val plan = new DefaultTicketSerializationExecutionPlan();
            providers.forEach(provider -> provider.configureTicketSerialization(plan));
            return plan;
        }
    }

    @Configuration(value = "CasCoreTicketsSerializationManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreTicketsSerializationManagementConfiguration {

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "ticketSerializationManager")
        public TicketSerializationManager ticketSerializationManager(
            @Qualifier("ticketSerializationExecutionPlan")
            final TicketSerializationExecutionPlan ticketSerializationExecutionPlan) {
            return new DefaultTicketStringSerializationManager(ticketSerializationExecutionPlan);
        }
    }

}
