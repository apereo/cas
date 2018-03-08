package org.apereo.cas.config;

import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.spring.data.documentdb.DocumentDbFactory;
import com.microsoft.azure.spring.data.documentdb.core.DocumentDbTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbServiceRegistryProperties;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.CosmosDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
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
        final CosmosDbObjectFactory factory = new CosmosDbObjectFactory(this.applicationContext);
        final CosmosDbServiceRegistryProperties cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        final DocumentDbFactory dbFactory = factory.createDocumentDbFactory(cosmosDb);
        final DocumentDbTemplate db = factory.createDocumentDbTemplate(dbFactory, cosmosDb);

        if (cosmosDb.isDropCollection()) {
            final String collectionLink = CosmosDbObjectFactory.getCollectionLink(cosmosDb.getDatabase(), cosmosDb.getCollection());
            final RequestOptions options = new RequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.valueOf(cosmosDb.getConsistencyLevel()));
            options.setOfferThroughput(cosmosDb.getThroughput());
            try {
                dbFactory.getDocumentClient().deleteCollection(collectionLink, options);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        db.createCollectionIfNotExists(cosmosDb.getCollection(), PARTITION_KEY_FIELD_NAME, cosmosDb.getThroughput());
        return new CosmosDbServiceRegistry(db, dbFactory, cosmosDb.getCollection(), cosmosDb.getDatabase());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(cosmosDbServiceRegistry());
    }
}
