package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultSecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasWsSecurityTokenTicketComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casWsSecurityTokenTicketComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWsSecurityTokenTicketComponentSerializationConfiguration {

    @Bean
    public TicketSerializationExecutionPlanConfigurer casWsSecurityTokenTicketSerializationExecutionPlanConfigurer() {
        return plan -> {
            plan.registerTicketSerializer(new SecurityTokenTicketStringSerializer());
            plan.registerTicketSerializer(SecurityTokenTicket.class.getName(), new SecurityTokenTicketStringSerializer());
        };
    }

    @Bean
    public ComponentSerializationPlanConfigurer casWsSecurityTokenComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(DefaultSecurityTokenTicket.class);
    }

    private static class SecurityTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<DefaultSecurityTokenTicket> {
        private static final long serialVersionUID = -3198623586274810263L;

        @Override
        public Class<DefaultSecurityTokenTicket> getTypeToSerialize() {
            return DefaultSecurityTokenTicket.class;
        }
    }
}
