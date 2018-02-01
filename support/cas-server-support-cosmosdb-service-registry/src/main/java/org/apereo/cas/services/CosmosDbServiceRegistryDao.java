package org.apereo.cas.services;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.spring.data.documentdb.DocumentDbFactory;
import com.microsoft.azure.spring.data.documentdb.core.DocumentDbTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.cosmosdb.CosmosDbDocument;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is {@link CosmosDbServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CosmosDbServiceRegistryDao extends AbstractServiceRegistryDao {
    private static final String PARTITION_KEY_FIELD_VALUE = "CasServices";

    private final DocumentDbTemplate documentDbTemplate;
    private final DocumentDbFactory documentDbFactory;
    private final String collectionName;
    private final String databaseName;

    private final StringSerializer<RegisteredService> serializer;

    public CosmosDbServiceRegistryDao(final DocumentDbTemplate db, final DocumentDbFactory dbFactory,
                                      final String collectionName, final String databaseName) {
        this.documentDbTemplate = db;
        this.collectionName = collectionName;
        this.documentDbFactory = dbFactory;
        this.databaseName = databaseName;
        this.serializer = new DefaultRegisteredServiceJsonSerializer(new MinimalPrettyPrinter());
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) registeredService).setId(System.currentTimeMillis());
            insert(registeredService);
        } else {
            update(registeredService);
        }

        return registeredService;
    }

    private void insert(final RegisteredService registeredService) {
        final CosmosDbDocument document = createCosmosDbDocument(registeredService);
        this.documentDbTemplate.insert(this.collectionName, document, document.getPartitionKey());
    }

    private void update(final RegisteredService registeredService) {
        try {
            final CosmosDbDocument document = createCosmosDbDocument(registeredService);
            final String id = String.valueOf(registeredService.getId());
            this.documentDbTemplate.update(this.collectionName, document, id, document.getPartitionKey());
        } catch (final Exception e) {
            if (e.getCause().getClass().equals(DocumentClientException.class)) {
                final DocumentClientException ex = DocumentClientException.class.cast(e.getCause());
                if (ex.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    insert(registeredService);
                }
            }
        }
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        final String id = String.valueOf(registeredService.getId());
        this.documentDbTemplate.deleteById(this.collectionName, id, Document.class, PARTITION_KEY_FIELD_VALUE);
        return true;
    }

    @Override
    public List<RegisteredService> load() {
        final String query = String.format("SELECT * FROM %s c", this.collectionName);
        final FeedResponse<Document> results = queryDocuments(query);
        final Iterator<Document> it = results.getQueryIterator();

        final List<RegisteredService> services = new ArrayList<>();
        while (it.hasNext()) {
            final Document doc = it.next();
            final RegisteredService svc = getRegisteredServiceFromDocumentBody(doc);
            if (svc != null) {
                services.add(svc);
            }
        }
        return services;
    }

    private RegisteredService getRegisteredServiceFromDocumentBody(final Document doc) {
        if (doc != null) {
            final String body = doc.getString("body");
            if (StringUtils.isNotBlank(body)) {
                return this.serializer.from(body);
            }
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        final CosmosDbDocument doc = this.documentDbTemplate.findById(this.collectionName, String.valueOf(id),
                CosmosDbDocument.class, PARTITION_KEY_FIELD_VALUE);
        if (doc != null) {
            return this.serializer.from(doc.getBody());
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        final String query = String.format("SELECT * FROM %s c WHERE CONTAINS(c.body,'%s')", this.collectionName, id);
        final FeedResponse<Document> results = queryDocuments(query);
        final Iterator<Document> it = results.getQueryIterator();
        if (it.hasNext()) {
            final Document doc = it.next();
            return getRegisteredServiceFromDocumentBody(doc);
        }
        return null;
    }

    private FeedResponse<Document> queryDocuments(final String query) {
        final SqlQuerySpec sql = new SqlQuerySpec(query);
        final FeedOptions feed = new FeedOptions();
        feed.setEnableCrossPartitionQuery(true);
        final DocumentClient documentClient = this.documentDbFactory.getDocumentClient();
        final String collectionLink = CosmosDbObjectFactory.getCollectionLink(this.databaseName, this.collectionName);
        return documentClient.queryDocuments(collectionLink, sql, feed, PARTITION_KEY_FIELD_VALUE);
    }

    private CosmosDbDocument createCosmosDbDocument(final RegisteredService registeredService) {
        final String body = serializer.toString(registeredService);
        final CosmosDbDocument document = new CosmosDbDocument();
        document.setPartitionKey(PARTITION_KEY_FIELD_VALUE);
        document.setBody(body);
        document.setId(String.valueOf(registeredService.getId()));
        return document;
    }
}
