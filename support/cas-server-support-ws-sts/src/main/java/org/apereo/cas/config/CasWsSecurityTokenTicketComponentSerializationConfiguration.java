package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.DefaultSecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasWsSecurityTokenTicketComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederationIdentityProvider)
@Configuration(value = "CasWsSecurityTokenTicketComponentSerializationConfiguration", proxyBeanMethods = false)
class CasWsSecurityTokenTicketComponentSerializationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketSerializationExecutionPlanConfigurer casWsSecurityTokenTicketSerializationExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        return plan -> {
            plan.registerTicketSerializer(new SecurityTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(SecurityTokenTicket.class.getName(), new SecurityTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(SecurityTokenTicket.PREFIX, new SecurityTokenTicketStringSerializer(applicationContext));
        };
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer casWsSecurityTokenComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(DefaultSecurityTokenTicket.class);
    }

    private static final class SecurityTokenTicketStringSerializer extends BaseJacksonSerializer<DefaultSecurityTokenTicket> {
        @Serial
        private static final long serialVersionUID = -3198623586274810263L;

        SecurityTokenTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, DefaultSecurityTokenTicket.class);
        }
    }
}
