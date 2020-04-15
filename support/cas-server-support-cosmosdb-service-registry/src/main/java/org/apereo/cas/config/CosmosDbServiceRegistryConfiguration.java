package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.CosmosDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

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
@Configuration(value = "cosmosDbServiceRegistryConfiguration", proxyBeanMethods = true)
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
    public DocumentDbTemplate cosmosDbDocumentDbTemplate() {
        val factory = cosmosDbObjectFactory();
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        val dbFactory = factory.createDocumentDbFactory(cosmosDb);
        return factory.createDocumentDbTemplate(dbFactory, cosmosDb);
    }

    @ConditionalOnMissingBean(name = "cosmosDbDocumentDbFactory")
    @Bean
    public DocumentDbFactory cosmosDbDocumentDbFactory() {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        return cosmosDbObjectFactory().createDocumentDbFactory(cosmosDb);
    }

    @Bean
    @RefreshScope
    public ServiceRegistry cosmosDbServiceRegistry() {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        val dbFactory = cosmosDbDocumentDbFactory();

        if (cosmosDb.isDropCollection()) {
            val collectionLink = CosmosDbObjectFactory.getCollectionLink(cosmosDb.getDatabase(), cosmosDb.getCollection());
            val options = new RequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.valueOf(cosmosDb.getConsistencyLevel()));
            options.setOfferThroughput(cosmosDb.getThroughput());
            try {
                dbFactory.getDocumentClient().deleteCollection(collectionLink, options);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        val indexingPolicy = new IndexingPolicy();
        indexingPolicy.setAutomatic(true);
        indexingPolicy.setIndexingMode(IndexingMode.valueOf(cosmosDb.getIndexingMode()));

        val db = cosmosDbDocumentDbTemplate();
        db.createCollectionIfNotExists(cosmosDb.getCollection(), PARTITION_KEY_FIELD_NAME,
            cosmosDb.getThroughput(), indexingPolicy);
        return new CosmosDbServiceRegistry(db, dbFactory, cosmosDb.getCollection(),
            cosmosDb.getDatabase(), applicationContext, serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cosmosDbServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer cosmosDbServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(cosmosDbServiceRegistry());
    }

}
