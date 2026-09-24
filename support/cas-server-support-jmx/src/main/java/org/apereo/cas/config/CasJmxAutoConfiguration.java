package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.jmx.authentication.AuthenticationManagedResource;
import org.apereo.cas.jmx.authentication.PrincipalAttributesCacheManagedResource;
import org.apereo.cas.jmx.services.ServicesManagerManagedResource;
import org.apereo.cas.jmx.ticket.TicketRegistryManagedResource;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableMBeanExport;

/**
 * This is {@link CasJmxAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableMBeanExport
@EnableAspectJAutoProxy(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core, module = "jmx")
@AutoConfiguration
public class CasJmxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "servicesManagerManagedResource")
    public ServicesManagerManagedResource servicesManagerManagedResource(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new ServicesManagerManagedResource(servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryManagedResource")
    public TicketRegistryManagedResource ticketRegistryManagedResource(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(TicketRegistryCleaner.BEAN_NAME)
        final ObjectProvider<TicketRegistryCleaner> ticketRegistryCleaner) {
        return new TicketRegistryManagedResource(ticketRegistry, ticketRegistryCleaner);
    }

    @Bean
    @ConditionalOnMissingBean(name = "authenticationManagedResource")
    public AuthenticationManagedResource authenticationManagedResource(
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ObjectProvider<ServicesManager> servicesManager,
        final ApplicationContext applicationContext) {
        return new AuthenticationManagedResource(authenticationEventExecutionPlan, servicesManager, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "principalAttributesCacheManagedResource")
    public PrincipalAttributesCacheManagedResource principalAttributesCacheManagedResource(
        @Qualifier(PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME)
        final ObjectProvider<PrincipalAttributesRepositoryCache> principalAttributesRepositoryCache) {
        return new PrincipalAttributesCacheManagedResource(principalAttributesRepositoryCache);
    }
}
