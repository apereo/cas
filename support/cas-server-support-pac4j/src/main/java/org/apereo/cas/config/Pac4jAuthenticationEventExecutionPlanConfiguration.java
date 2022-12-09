package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.DelegatedAuthenticationAuditResourceResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.DefaultDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.provision.ChainingDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.GroovyDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.RestfulDelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.authentication.clients.DefaultDelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.support.pac4j.authentication.clients.RefreshableDelegatedClients;
import org.apereo.cas.support.pac4j.authentication.clients.RestfulDelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandler;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is {@link Pac4jAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@AutoConfiguration
public class Pac4jAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanSessionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanSessionConfiguration {
        @ConditionalOnMissingBean(name = "delegatedClientDistributedSessionStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SessionStore delegatedClientDistributedSessionStore(
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedClientDistributedSessionCookieGenerator")
            final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry) {
            val replicationProps = casProperties.getAuthn().getPac4j().getCore().getSessionReplication();
            if (replicationProps.isReplicateSessions()) {
                return new DistributedJEESessionStore(ticketRegistry,
                    ticketFactory, delegatedClientDistributedSessionCookieGenerator);
            }
            return JEESessionStore.INSTANCE;
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanCoreConfiguration {

        @ConditionalOnMissingBean(name = "delegatedClientDistributedSessionCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder delegatedClientDistributedSessionCookieGenerator(
            final CasConfigurationProperties casProperties) {
            val cookie = casProperties.getAuthn().getPac4j().getCore().getSessionReplication().getCookie();
            return CookieUtils.buildCookieRetrievingGenerator(cookie);
        }

        @ConditionalOnMissingBean(name = "clientPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory clientPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanMetadataConfiguration {
        @ConditionalOnMissingBean(name = "clientAuthenticationMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
            return new ClientAuthenticationMetaDataPopulator();
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanHandlerConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "clientAuthenticationHandler")
        public AuthenticationHandler clientAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("clientPrincipalFactory")
            final PrincipalFactory clientPrincipalFactory,
            @Qualifier("builtClients")
            final Clients builtClients,
            @Qualifier(DelegatedClientUserProfileProvisioner.BEAN_NAME)
            final DelegatedClientUserProfileProvisioner clientUserProfileProvisioner,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val pac4j = casProperties.getAuthn().getPac4j().getCore();
            val handler = new DelegatedClientAuthenticationHandler(pac4j, 
                servicesManager, clientPrincipalFactory, builtClients, clientUserProfileProvisioner,
                delegatedClientDistributedSessionStore, applicationContext);
            handler.setTypedIdUsed(pac4j.isTypedIdUsed());
            handler.setPrincipalAttributeId(pac4j.getPrincipalIdAttribute());
            return handler;
        }

    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "groovyDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> groovyDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.groovy.location").exists().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
                    val script = provisioning.getGroovy().getLocation();
                    return () -> new GroovyDelegatedClientUserProfileProvisioner(script);
                })
                .otherwise(() -> DelegatedClientUserProfileProvisioner::noOp)
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restDelegatedClientUserProfileProvisioner")
        public Supplier<DelegatedClientUserProfileProvisioner> restDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.rest.url").exists().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
                    return () -> new RestfulDelegatedClientUserProfileProvisioner(provisioning.getRest());
                })
                .otherwise(() -> DelegatedClientUserProfileProvisioner::noOp)
                .get();
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanProvisionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanProvisionConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = DelegatedClientUserProfileProvisioner.BEAN_NAME)
        public DelegatedClientUserProfileProvisioner clientUserProfileProvisioner(
            final ObjectProvider<List<Supplier<DelegatedClientUserProfileProvisioner>>> provisioners) {
            val results = provisioners.getIfAvailable(() -> CollectionUtils.wrapList(DelegatedClientUserProfileProvisioner::noOp))
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .map(Supplier::get)
                .collect(Collectors.toList());
            return new ChainingDelegatedClientUserProfileProvisioner(results);
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanClientFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanClientFactoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "pac4jDelegatedClientNameExtractor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientNameExtractor pac4jDelegatedClientNameExtractor() {
            return DelegatedClientNameExtractor.fromHttpRequest();
        }

        @Bean
        @ConditionalOnMissingBean(name = "pac4jDelegatedClientFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientFactory pac4jDelegatedClientFactory(
            @Qualifier(DelegatedClientFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
            final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
            final CasConfigurationProperties casProperties,
            final ObjectProvider<List<DelegatedClientFactoryCustomizer>> customizerList,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {

            val core = casProperties.getAuthn().getPac4j().getCore();
            val clientsCache = Caffeine.newBuilder()
                .maximumSize(core.getCacheSize())
                .expireAfterAccess(Beans.newDuration(core.getCacheDuration()))
                .<String, Collection<IndirectClient>>build();

            val customizers = Optional.ofNullable(customizerList.getIfAvailable())
                .map(result -> {
                    AnnotationAwareOrderComparator.sortIfNecessary(result);
                    return result;
                }).orElseGet(() -> new ArrayList<>(0));

            if (StringUtils.isNotBlank(casProperties.getAuthn().getPac4j().getRest().getUrl())) {
                return new RestfulDelegatedClientFactory(customizers, casSslContext,
                    casProperties, samlMessageStoreFactory, clientsCache);
            }
            return new DefaultDelegatedClientFactory(casProperties,
                customizers, casSslContext, samlMessageStoreFactory, clientsCache);
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanClientConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "delegatedAuthenticationCredentialExtractor")
        public DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor(
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return new DefaultDelegatedAuthenticationCredentialExtractor(delegatedClientDistributedSessionStore);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "builtClients")
        public Clients builtClients(final CasConfigurationProperties casProperties,
                                    @Qualifier("pac4jDelegatedClientFactory")
                                    final DelegatedClientFactory pac4jDelegatedClientFactory) {
            return new RefreshableDelegatedClients(casProperties.getServer().getLoginUrl(), pac4jDelegatedClientFactory);
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanAuditConfiguration {
        @ConditionalOnMissingBean(name = "delegatedAuthenticationAuditResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver delegatedAuthenticationAuditResourceResolver() {
            return new DelegatedAuthenticationAuditResourceResolver();
        }

    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanAuditPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanAuditPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "delegatedAuthenticationAuditTrailRecordResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer delegatedAuthenticationAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("delegatedAuthenticationAuditResourceResolver")
            final AuditResourceResolver delegatedAuthenticationAuditResourceResolver,
            @Qualifier("authenticationActionResolver")
            final AuditActionResolver authenticationActionResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.DELEGATED_CLIENT_ACTION_RESOLVER, authenticationActionResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.DELEGATED_CLIENT_RESOURCE_RESOLVER, delegatedAuthenticationAuditResourceResolver);
            };
        }

    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanLogoutConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedAuthenticationLogoutExecutionPlanConfigurer")
        public LogoutExecutionPlanConfigurer delegatedAuthenticationLogoutExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return plan -> {
                val replicate = casProperties.getAuthn().getPac4j().getCore().getSessionReplication().isReplicateSessions();
                if (replicate) {
                    plan.registerLogoutPostProcessor(ticketGrantingTicket -> {
                        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
                        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
                        if (request != null && response != null) {
                            delegatedClientDistributedSessionStore.destroySession(new JEEContext(request, response));
                        }
                    });
                }
            };
        }

    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "pac4jAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer pac4jAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("builtClients")
            final Clients builtClients,
            @Qualifier("clientAuthenticationHandler")
            final AuthenticationHandler clientAuthenticationHandler,
            @Qualifier("clientAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> {
                if (!builtClients.findAllClients().isEmpty()) {
                    LOGGER.info("Registering delegated authentication clients...");
                    plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler, defaultPrincipalResolver);
                    plan.registerAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
                }
            };
        }
    }
}
