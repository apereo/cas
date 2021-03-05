package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.CosmosDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.LoggingUtils;

import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.IndexingMode;
import com.microsoft.azure.documentdb.IndexingPolicy;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.spring.data.documentdb.DocumentDbFactory;
import com.microsoft.azure.spring.data.documentdb.core.DocumentDbTemplate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link CosmosDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "cosmosDbServiceRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CosmosDbServiceRegistryConfiguration {
    /**
     * Partition key field name.
     */
    private static final String PARTITION_KEY_FIELD_NAME = "partitionKey";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @ConditionalOnMissingBean(name = "cosmosDbObjectFactory")
    @Bean
    public CosmosDbObjectFactory cosmosDbObjectFactory() {
        return new CosmosDbObjectFactory(this.applicationContext);
    }

    @ConditionalOnMissingBean(name = "cosmosDbDocumentDbTemplate")
    @Bean
    public DocumentDbTemplate cosmosDbDocumentDbTemplate(@Qualifier("cosmosDbObjectFactory") final CosmosDbObjectFactory cosmosDbObjectFactory) {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        val dbFactory = cosmosDbObjectFactory.createDocumentDbFactory(cosmosDb);
        return cosmosDbObjectFactory.createDocumentDbTemplate(dbFactory, cosmosDb);
    }

    @ConditionalOnMissingBean(name = "cosmosDbDocumentDbFactory")
    @Bean
    @Autowired
    public DocumentDbFactory cosmosDbDocumentDbFactory(@Qualifier("cosmosDbObjectFactory") final CosmosDbObjectFactory cosmosDbObjectFactory) {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        return cosmosDbObjectFactory.createDocumentDbFactory(cosmosDb);
    }

    @Bean
    @RefreshScope
    @Autowired
    public ServiceRegistry cosmosDbServiceRegistry(
        @Qualifier("cosmosDbDocumentDbTemplate") final DocumentDbTemplate cosmosDbDocumentDbTemplate,
        @Qualifier("cosmosDbDocumentDbFactory") final DocumentDbFactory cosmosDbDocumentDbFactory) {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();

        if (cosmosDb.isDropCollection()) {
            val collectionLink = CosmosDbObjectFactory.getCollectionLink(cosmosDb.getDatabase(), cosmosDb.getCollection());
            val options = new RequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.valueOf(cosmosDb.getConsistencyLevel()));
            options.setOfferThroughput(cosmosDb.getThroughput());
            try {
                cosmosDbDocumentDbFactory.getDocumentClient().deleteCollection(collectionLink, options);
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        val indexingPolicy = new IndexingPolicy();
        indexingPolicy.setAutomatic(true);
        indexingPolicy.setIndexingMode(IndexingMode.valueOf(cosmosDb.getIndexingMode()));

        cosmosDbDocumentDbTemplate.createCollectionIfNotExists(cosmosDb.getCollection(), PARTITION_KEY_FIELD_NAME,
            cosmosDb.getThroughput(), indexingPolicy);
        return new CosmosDbServiceRegistry(cosmosDbDocumentDbTemplate, cosmosDbDocumentDbFactory, cosmosDb.getCollection(),
            cosmosDb.getDatabase(), applicationContext, serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cosmosDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer cosmosDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("cosmosDbServiceRegistry") final ServiceRegistry cosmosDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(cosmosDbServiceRegistry);
    }

}
