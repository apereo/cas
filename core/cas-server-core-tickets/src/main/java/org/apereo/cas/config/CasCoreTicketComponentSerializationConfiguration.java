package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.DefaultEncodedTicket;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreTicketComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casCoreComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreTicketComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "coreTicketsComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer coreTicketsComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(RememberMeDelegatingExpirationPolicy.class);

            plan.registerSerializableClass(TicketGrantingTicketImpl.class);
            plan.registerSerializableClass(ServiceTicketImpl.class);
            plan.registerSerializableClass(ProxyGrantingTicketImpl.class);
            plan.registerSerializableClass(ProxyTicketImpl.class);
            plan.registerSerializableClass(DefaultEncodedTicket.class);
            plan.registerSerializableClass(TransientSessionTicketImpl.class);

            plan.registerSerializableClass(MultiTimeUseOrTimeoutExpirationPolicy.class);
            plan.registerSerializableClass(MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy.class);
            plan.registerSerializableClass(MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy.class);
            plan.registerSerializableClass(NeverExpiresExpirationPolicy.class);
            plan.registerSerializableClass(RememberMeDelegatingExpirationPolicy.class);
            plan.registerSerializableClass(TimeoutExpirationPolicy.class);
            plan.registerSerializableClass(HardTimeoutExpirationPolicy.class);
            plan.registerSerializableClass(AlwaysExpiresExpirationPolicy.class);
            plan.registerSerializableClass(ThrottledUseAndTimeoutExpirationPolicy.class);
            plan.registerSerializableClass(TicketGrantingTicketExpirationPolicy.class);
            plan.registerSerializableClass(BaseDelegatingExpirationPolicy.class);
            
        };
    }
}
