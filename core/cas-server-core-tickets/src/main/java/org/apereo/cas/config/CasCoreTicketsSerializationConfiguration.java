package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.serialization.DefaultTicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.DefaultTicketStringSerializationManager;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry)
@Configuration(value = "CasCoreTicketsSerializationConfiguration", proxyBeanMethods = false)
class CasCoreTicketsSerializationConfiguration {

    @Configuration(value = "CasCoreTicketsSerializationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreTicketsSerializationPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "ticketSerializationExecutionPlan")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketSerializationExecutionPlan ticketSerializationExecutionPlan(
            final ObjectProvider<List<TicketSerializationExecutionPlanConfigurer>> providerList) {
            val providers = Optional.ofNullable(providerList.getIfAvailable()).orElseGet(ArrayList::new);
            AnnotationAwareOrderComparator.sort(providers);
            val plan = new DefaultTicketSerializationExecutionPlan();
            providers.forEach(provider -> provider.configureTicketSerialization(plan));
            return plan;
        }
    }

    @Configuration(value = "CasCoreTicketsSerializationManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreTicketsSerializationManagementConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = TicketSerializationManager.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketSerializationManager ticketSerializationManager(
            @Qualifier("ticketSerializationExecutionPlan") final TicketSerializationExecutionPlan ticketSerializationExecutionPlan) {
            return new DefaultTicketStringSerializationManager(ticketSerializationExecutionPlan);
        }
    }

}
