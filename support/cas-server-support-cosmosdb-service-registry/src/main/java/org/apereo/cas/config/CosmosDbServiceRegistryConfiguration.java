package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.CosmosDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;

import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.IndexingMode;
import com.microsoft.azure.documentdb.IndexingPolicy;
import com.microsoft.azure.documentdb.RequestOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CosmosDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("cosmosDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CosmosDbServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {
    /**
     * Partition key field name.
     */
    private static final String PARTITION_KEY_FIELD_NAME = "partitionKey";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @RefreshScope
    public ServiceRegistry cosmosDbServiceRegistry() {
        val factory = new CosmosDbObjectFactory(this.applicationContext);
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        val dbFactory = factory.createDocumentDbFactory(cosmosDb);
        val db = factory.createDocumentDbTemplate(dbFactory, cosmosDb);

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
        db.createCollectionIfNotExists(cosmosDb.getCollection(), PARTITION_KEY_FIELD_NAME,
            cosmosDb.getThroughput(), indexingPolicy);
        return new CosmosDbServiceRegistry(db, dbFactory, cosmosDb.getCollection(), cosmosDb.getDatabase());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(cosmosDbServiceRegistry());
    }
}
