package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mongo.CasMongoOperations;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DelegatedAuthenticationSaml2WebflowConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientFinishLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientTerminateSessionAction;
import org.apereo.cas.web.saml2.DelegatedAuthenticationSamlIdPResponseCustomizer;
import org.apereo.cas.web.saml2.DelegatedClientSaml2Builder;
import org.apereo.cas.web.saml2.DelegatedClientsSaml2EndpointContributor;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataController;
import com.hazelcast.core.HazelcastInstance;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.jdbc.SAML2JdbcMetadataGenerator;
import org.pac4j.saml.metadata.mongo.SAML2MongoMetadataGenerator;
import org.pac4j.saml.store.HazelcastSAMLMessageStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import java.util.List;

/**
 * This is {@link DelegatedAuthenticationSaml2Configuration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml")
@Configuration(value = "DelegatedAuthenticationSaml2Configuration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedAuthenticationSaml2Configuration {

    @ConditionalOnClass(SamlIdPResponseCustomizer.class)
    @Configuration(value = "DelegatedAuthenticationSAML2IdPConfiguration", proxyBeanMethods = false)
    static class DelegatedAuthenticationSAML2IdPConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2IdPResponseCustomizer")
        public SamlIdPResponseCustomizer delegatedSaml2IdPResponseCustomizer(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders) {
            return new DelegatedAuthenticationSamlIdPResponseCustomizer(identityProviders);
        }
    }

    @ConditionalOnClass(HazelcastInstance.class)
    @Configuration(value = "DelegatedAuthenticationSAMLHazelcastConfiguration", proxyBeanMethods = false)
    static class DelegatedAuthenticationSAMLHazelcastConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnBean(name = "casTicketRegistryHazelcastInstance")
        @ConditionalOnMissingBean(name = DelegatedIdentityProviderFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory(
            @Qualifier("casTicketRegistryHazelcastInstance") final ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance) {
            return new HazelcastSAMLMessageStoreFactory(casTicketRegistryHazelcastInstance.getObject());
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml-jdbc", enabledByDefault = false)
    @Configuration(value = "DelegatedAuthenticationSAMLJdbcConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(JpaBeanFactory.class)
    static class DelegatedAuthenticationSAMLJdbcConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientJdbcMetadataCustomizer")
        public DelegatedClientFactoryCustomizer delegatedSaml2ClientJdbcMetadataCustomizer(
            final CasConfigurationProperties casProperties) {
            return client -> {
                if (client instanceof final SAML2Client saml2Client) {
                    val configuration = saml2Client.getConfiguration();
                    casProperties.getAuthn().getPac4j().getSaml()
                        .stream()
                        .map(saml -> saml.getMetadata().getServiceProvider().getJdbc())
                        .filter(saml -> StringUtils.isNotBlank(saml.getUrl()) && StringUtils.isNotBlank(saml.getTableName()))
                        .forEach(saml -> {
                            val datasource = JpaBeans.newDataSource(saml);
                            val metadataGenerator = new SAML2JdbcMetadataGenerator(new JdbcTemplate(datasource), configuration.getServiceProviderEntityId());
                            metadataGenerator.setTableName(saml.getTableName());
                            configuration.setServiceProviderMetadataResource(ResourceUtils.NULL_RESOURCE);
                            configuration.setMetadataGenerator(metadataGenerator);
                        });
                }
            };
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml-mongodb", enabledByDefault = false)
    @Configuration(value = "DelegatedAuthenticationSAMLMongoDbConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(CasMongoOperations.class)
    static class DelegatedAuthenticationSAMLMongoDbConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientMongoDbMetadataCustomizer")
        public DelegatedClientFactoryCustomizer delegatedSaml2ClientMongoDbMetadataCustomizer(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
            return client -> {
                if (client instanceof final SAML2Client saml2Client) {
                    val configuration = saml2Client.getConfiguration();
                    casProperties.getAuthn().getPac4j().getSaml()
                        .stream()
                        .map(saml -> saml.getMetadata().getServiceProvider().getMongo())
                        .filter(saml -> StringUtils.isNotBlank(saml.getCollection()))
                        .forEach(mongo -> {
                            val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                            val mongoClient = factory.buildMongoDbClient(mongo);
                            val mongoTemplate = factory.buildMongoTemplate(mongoClient, mongo);
                            MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
                            val metadataGenerator = new SAML2MongoMetadataGenerator(mongoClient, configuration.getServiceProviderEntityId());
                            metadataGenerator.setMetadataCollection(mongo.getCollection());
                            metadataGenerator.setMetadataDatabase(mongo.getDatabaseName());
                            configuration.setServiceProviderMetadataResource(ResourceUtils.NULL_RESOURCE);
                            configuration.setMetadataGenerator(metadataGenerator);
                        });
                }
            };
        }
    }

    @Configuration(value = "DelegatedAuthenticationSAMLWebConfiguration", proxyBeanMethods = false)
    static class DelegatedAuthenticationSAMLWebConfiguration {
        
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_TERMINATE_SESSION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedSaml2ClientTerminateSessionAction(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(Action.class)
                .when(BeanCondition.on("cas.slo.disabled").isFalse().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> WebflowActionBeanSupplier.builder()
                    .withApplicationContext(applicationContext)
                    .withProperties(casProperties)
                    .withAction(() -> new DelegatedSaml2ClientTerminateSessionAction(identityProviders, delegatedClientDistributedSessionStore))
                    .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_TERMINATE_SESSION)
                    .build()
                    .get())
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }


        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedSaml2ClientLogoutAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
            final SingleLogoutRequestExecutor singleLogoutRequestExecutor) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedSaml2ClientLogoutAction(ticketRegistry, singleLogoutRequestExecutor))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
                .build()
                .get();
        }


        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationSaml2ClientFinishLogoutAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders,
            @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedSaml2ClientFinishLogoutAction(identityProviders, delegatedClientDistributedSessionStore))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT)
                .build()
                .get();
        }


        @ConditionalOnMissingBean(name = "delegatedAuthenticationSaml2WebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer delegatedAuthenticationSaml2WebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry logoutFlowDefinitionRegistry) {
            return new DelegatedAuthenticationSaml2WebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                logoutFlowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedAuthenticationSaml2WebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer delegatedAuthenticationSaml2WebflowExecutionPlanConfigurer(
            @Qualifier("delegatedAuthenticationSaml2WebflowConfigurer")
            final CasWebflowConfigurer delegatedAuthenticationSaml2WebflowConfigurer,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .alwaysMatch()
                .supply(() -> plan -> plan.registerWebflowConfigurer(delegatedAuthenticationSaml2WebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientSaml2EndpointConfigurer")
        public CasWebSecurityConfigurer<Void> delegatedClientSaml2EndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT, "/"),
                        StringUtils.prependIfMissing(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER, "/"));
                }
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientMetadataController")
        public DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME) final OpenSamlConfigBean configBean) {
            return new DelegatedSaml2ClientMetadataController(identityProviders, configBean);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientsSaml2EndpointContributor")
        public DelegatedClientsEndpointContributor delegatedClientsSaml2EndpointContributor() {
            return new DelegatedClientsSaml2EndpointContributor();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientBuilder")
        public ConfigurableDelegatedClientBuilder delegatedSaml2ClientBuilder(
            @Qualifier(DelegatedIdentityProviderFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
            final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext,
            final CasConfigurationProperties casProperties) {
            return new DelegatedClientSaml2Builder(casSslContext, samlMessageStoreFactory, casProperties);
        }
    }
}
