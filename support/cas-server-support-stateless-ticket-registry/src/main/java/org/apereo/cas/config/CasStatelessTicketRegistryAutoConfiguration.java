package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.ShortenedServiceMatchingStrategy;
import org.apereo.cas.ticket.registry.StatelessTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.compact.CompactTicketAuthentication;
import org.apereo.cas.ticket.registry.compact.ProxyGrantingTicketCompactor;
import org.apereo.cas.ticket.registry.compact.ProxyTicketCompactor;
import org.apereo.cas.ticket.registry.compact.ServiceTicketCompactor;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.ticket.registry.compact.TicketGrantingTicketCompactor;
import org.apereo.cas.ticket.registry.compact.TransientSessionTicketCompactor;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasStatelessTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "stateless")
@AutoConfiguration
public class CasStatelessTicketRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "statelessTicketRegistryCipherExecutor")
    public CipherExecutor statelessTicketRegistryCipherExecutor(final CasConfigurationProperties casProperties) {
        val stateless = casProperties.getTicket().getRegistry().getStateless();
        return CoreTicketUtils.newTicketRegistryCipherExecutor(stateless.getCrypto(), "stateless");
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "statelessTicketRegistry")
    public TicketRegistry ticketRegistry(
        @Qualifier("statelessTicketRegistryCipherExecutor")
        final CipherExecutor statelessTicketRegistryCipherExecutor,
        final List<TicketCompactor<? extends Ticket>> ticketCompactors,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        final ConfigurableApplicationContext applicationContext) {
        return new StatelessTicketRegistry(statelessTicketRegistryCipherExecutor, ticketSerializationManager, ticketCatalog,
            applicationContext, ticketCompactors);
    }

    @ConditionalOnMissingBean(name = "ticketGrantingTicketCompactor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCompactor ticketGrantingTicketCompactor(
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final ObjectProvider<PrincipalResolver> principalResolver,
        @Qualifier(TicketFactory.BEAN_NAME)
        final ObjectProvider<TicketFactory> ticketFactory,
        final ConfigurableApplicationContext applicationContext) {
        val serializer = BaseJacksonSerializer.forType(applicationContext, Authentication.class);
        return new TicketGrantingTicketCompactor(ticketFactory, principalResolver, serializer);
    }

    @ConditionalOnMissingBean(name = "proxyGrantingTicketCompactor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCompactor proxyGrantingTicketCompactor(
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketFactory.BEAN_NAME)
        final ObjectProvider<TicketFactory> ticketFactory,
        @Qualifier(PrincipalFactory.BEAN_NAME)
        final PrincipalFactory principalFactory,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory serviceFactory) {
        return new ProxyGrantingTicketCompactor(ticketFactory, serviceFactory, principalFactory,
            CompactTicketAuthentication.getRetainedAuthenticationAttributes(casProperties));
    }

    @ConditionalOnMissingBean(name = "serviceTicketCompactor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCompactor serviceTicketCompactor(
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketFactory.BEAN_NAME)
        final ObjectProvider<TicketFactory> ticketFactory,
        @Qualifier(PrincipalFactory.BEAN_NAME)
        final PrincipalFactory principalFactory,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory serviceFactory) {
        return new ServiceTicketCompactor(ticketFactory, serviceFactory, principalFactory,
            CompactTicketAuthentication.getRetainedAuthenticationAttributes(casProperties));
    }

    @ConditionalOnMissingBean(name = "proxyTicketCompactor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCompactor proxyTicketCompactor(
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketFactory.BEAN_NAME)
        final ObjectProvider<TicketFactory> ticketFactory,
        @Qualifier(PrincipalFactory.BEAN_NAME)
        final PrincipalFactory principalFactory,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory serviceFactory) {
        return new ProxyTicketCompactor(ticketFactory, serviceFactory, principalFactory,
            CompactTicketAuthentication.getRetainedAuthenticationAttributes(casProperties));
    }

    @ConditionalOnMissingBean(name = "transientTicketCompactor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCompactor transientTicketCompactor(
        @Qualifier(TicketFactory.BEAN_NAME)
        final ObjectProvider<TicketFactory> ticketFactory,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory serviceFactory) {
        return new TransientSessionTicketCompactor(ticketFactory, serviceFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceMatchingStrategy serviceMatchingStrategy(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new ShortenedServiceMatchingStrategy(servicesManager);
    }
}
