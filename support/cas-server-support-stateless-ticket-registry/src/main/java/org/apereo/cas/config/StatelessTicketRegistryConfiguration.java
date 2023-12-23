package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.StatelessTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.StatelessTicketRegistryWebflowConfigurer;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link StatelessTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "stateless")
@AutoConfiguration
public class StatelessTicketRegistryConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "statelessTicketRegistryCipherExecutor")
    public CipherExecutor statelessTicketRegistryCipherExecutor(final CasConfigurationProperties casProperties) {
        val mem = casProperties.getTicket().getRegistry().getStateless();
        return CoreTicketUtils.newTicketRegistryCipherExecutor(mem.getCrypto(), "stateless");
    }

    @ConditionalOnMissingBean(name = TicketRegistry.BEAN_NAME)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(JwtBuilder.TICKET_JWT_BUILDER_BEAN_NAME)
        final JwtBuilder tokenTicketJwtBuilder,
        @Qualifier("statelessTicketRegistryCipherExecutor")
        final CipherExecutor defaultTicketRegistryCipherExecutor,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
        final ObjectProvider<LogoutManager> logoutManager,
        final CasConfigurationProperties casProperties) {
        return new StatelessTicketRegistry(defaultTicketRegistryCipherExecutor,
            ticketSerializationManager, ticketCatalog, tokenTicketJwtBuilder, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "statelessTicketRegistryWebflowExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer statelessTicketRegistryWebflowExecutionPlanConfigurer(
        @Qualifier("statelessTicketRegistryWebflowConfigurer")
        final CasWebflowConfigurer surrogateWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer);
    }

    @ConditionalOnMissingBean(name = "statelessTicketRegistryWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer statelessTicketRegistryWebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new StatelessTicketRegistryWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
    }
}
