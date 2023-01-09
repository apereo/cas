package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.mongo.CasMongoOperations;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.hazelcast.core.HazelcastInstance;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.jdbc.SAML2JdbcMetadataGenerator;
import org.pac4j.saml.metadata.mongo.SAML2MongoMetadataGenerator;
import org.pac4j.saml.store.HazelcastSAMLMessageStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is {@link DelegatedAuthenticationSAMLConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml")
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationSAMLConfiguration {

    @ConditionalOnClass(HazelcastInstance.class)
    @Configuration(value = "DelegatedAuthenticationSAMLHazelcastConfiguration", proxyBeanMethods = false)
    public static class DelegatedAuthenticationSAMLHazelcastConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnBean(name = "casTicketRegistryHazelcastInstance")
        @ConditionalOnMissingBean(name = DelegatedClientFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory(
            @Qualifier("casTicketRegistryHazelcastInstance")
            final ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance) {
            return new HazelcastSAMLMessageStoreFactory(casTicketRegistryHazelcastInstance.getObject());
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml-jdbc", enabledByDefault = false)
    @Configuration(value = "DelegatedAuthenticationSAMLJdbcConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(JpaBeanFactory.class)
    public static class DelegatedAuthenticationSAMLJdbcConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientJdbcMetadataCustomizer")
        public DelegatedClientFactoryCustomizer delegatedSaml2ClientJdbcMetadataCustomizer(
            final CasConfigurationProperties casProperties) {
            return client -> {
                if (client instanceof SAML2Client saml2Client) {
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
    public static class DelegatedAuthenticationSAMLMongoDbConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientMongoDbMetadataCustomizer")
        public DelegatedClientFactoryCustomizer delegatedSaml2ClientMongoDbMetadataCustomizer(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            return client -> {
                if (client instanceof SAML2Client saml2Client) {
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
}
