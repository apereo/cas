package org.apereo.cas.services;

import org.apereo.cas.cosmosdb.CosmosDbDocument;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.documentdb.PartitionKey;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.spring.data.documentdb.DocumentDbFactory;
import com.microsoft.azure.spring.data.documentdb.core.DocumentDbTemplate;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CosmosDbServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CosmosDbServiceRegistry extends AbstractServiceRegistry {
    private static final String PARTITION_KEY_FIELD_VALUE = "CasServices";

    private final DocumentDbTemplate documentDbTemplate;
    private final DocumentDbFactory documentDbFactory;
    private final String collectionName;
    private final String databaseName;

    private final StringSerializer<RegisteredService> serializer;

    public CosmosDbServiceRegistry(final DocumentDbTemplate db, final DocumentDbFactory dbFactory,
                                   final String collectionName, final String databaseName,
                                   final ConfigurableApplicationContext applicationContext,
                                   final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);

        this.documentDbTemplate = db;
        this.collectionName = collectionName;
        this.documentDbFactory = dbFactory;
        this.databaseName = databaseName;
        this.serializer = new RegisteredServiceJsonSerializer(new MinimalPrettyPrinter());
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        invokeServiceRegistryListenerPreSave(registeredService);
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            registeredService.setId(System.currentTimeMillis());
            insert(registeredService);
        } else {
            update(registeredService);
        }

        return registeredService;
    }

    private void insert(final RegisteredService registeredService) {
        val document = createCosmosDbDocument(registeredService);
        this.documentDbTemplate.insert(this.collectionName, document, new PartitionKey(document.getPartitionKey()));
    }

    private void update(final RegisteredService registeredService) {
        try {
            val document = createCosmosDbDocument(registeredService);
            val id = String.valueOf(registeredService.getId());
            this.documentDbTemplate.upsert(this.collectionName, document, id, new PartitionKey(document.getPartitionKey()));
        } catch (final Exception e) {
            if (e.getCause().getClass().equals(DocumentClientException.class)) {
                val ex = DocumentClientException.class.cast(e.getCause());
                if (ex.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    insert(registeredService);
                }
            }
        }
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val id = String.valueOf(registeredService.getId());
        this.documentDbTemplate.deleteById(this.collectionName, id, Document.class, new PartitionKey(PARTITION_KEY_FIELD_VALUE));
        return true;
    }

    @Override
    public Collection<RegisteredService> load() {
        val query = String.format("SELECT * FROM %s c", this.collectionName);
        val results = queryDocuments(query);
        val it = results.getQueryIterator();

        val services = new ArrayList<RegisteredService>();
        while (it.hasNext()) {
            val doc = it.next();
            var svc = getRegisteredServiceFromDocumentBody(doc);
            if (svc != null) {
                svc = invokeServiceRegistryListenerPostLoad(svc);
            }
            if (svc != null) {
                services.add(svc);
            }
        }
        return services;
    }

    private RegisteredService getRegisteredServiceFromDocumentBody(final Document doc) {
        if (doc != null) {
            val body = doc.getString("body");
            if (StringUtils.isNotBlank(body)) {
                return this.serializer.from(body);
            }
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val doc = this.documentDbTemplate.findById(this.collectionName, String.valueOf(id), CosmosDbDocument.class);
        if (doc != null) {
            return this.serializer.from(doc.getBody());
        }
        return null;
    }

    private FeedResponse<Document> queryDocuments(final String query) {
        val sql = new SqlQuerySpec(query);
        val feed = new FeedOptions();
        feed.setEnableCrossPartitionQuery(Boolean.TRUE);
        val documentClient = this.documentDbFactory.getDocumentClient();
        val collectionLink = CosmosDbObjectFactory.getCollectionLink(this.databaseName, this.collectionName);
        return documentClient.queryDocuments(collectionLink, sql, feed, PARTITION_KEY_FIELD_VALUE);
    }

    private CosmosDbDocument createCosmosDbDocument(final RegisteredService registeredService) {
        val body = serializer.toString(registeredService);
        val document = new CosmosDbDocument();
        document.setPartitionKey(PARTITION_KEY_FIELD_VALUE);
        document.setBody(body);
        document.setId(String.valueOf(registeredService.getId()));
        return document;
    }
}
