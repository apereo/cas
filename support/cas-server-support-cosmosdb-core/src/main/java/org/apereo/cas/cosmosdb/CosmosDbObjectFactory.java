package org.apereo.cas.cosmosdb;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.spring.data.documentdb.DocumentDbFactory;
import com.microsoft.azure.spring.data.documentdb.common.GetHashMac;
import com.microsoft.azure.spring.data.documentdb.core.DocumentDbTemplate;
import com.microsoft.azure.spring.data.documentdb.core.convert.MappingDocumentDbConverter;
import com.microsoft.azure.spring.data.documentdb.core.mapping.DocumentDbMappingContext;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.cosmosdb.BaseCosmosDbProperties;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;

/**
 * This is {@link CosmosDbObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class CosmosDbObjectFactory {
    private static final String USER_AGENT_SUFFIX = "spring-boot-starter/0.2.0";

    private final ApplicationContext applicationContext;

    /**
     * Create document client.
     *
     * @param properties the properties
     * @return the document client
     */
    public DocumentClient createDocumentClient(final BaseCosmosDbProperties properties) {
        final ConnectionPolicy policy = ConnectionPolicy.GetDefault();
        String userAgent = (policy.getUserAgentSuffix() == null
            ? StringUtils.EMPTY
            : ";" + policy.getUserAgentSuffix()) + ";" + USER_AGENT_SUFFIX;
        if (properties.isAllowTelemetry() && GetHashMac.getHashMac() != null) {
            userAgent += ";" + GetHashMac.getHashMac();
        }
        policy.setUserAgentSuffix(userAgent);
        return new DocumentClient(properties.getUri(), properties.getKey(), policy,
            ConsistencyLevel.valueOf(properties.getConsistencyLevel()));
    }

    /**
     * Create document db factory.
     *
     * @param properties the properties
     * @return the document db factory
     */
    public DocumentDbFactory createDocumentDbFactory(final BaseCosmosDbProperties properties) {
        final DocumentClient documentClient = createDocumentClient(properties);
        return new DocumentDbFactory(documentClient);
    }

    /**
     * Document db template.
     *
     * @param properties the properties
     * @return the document db template
     */
    public DocumentDbTemplate createDocumentDbTemplate(final BaseCosmosDbProperties properties) {
        final DocumentDbFactory documentDbFactory = createDocumentDbFactory(properties);
        final DocumentDbMappingContext documentDbMappingContext = createDocumentDbMappingContext();
        final MappingDocumentDbConverter mappingDocumentDbConverter = createMappingDocumentDbConverter(documentDbMappingContext);
        return new DocumentDbTemplate(documentDbFactory, mappingDocumentDbConverter, properties.getDatabase());
    }

    /**
     * Create document db template document db template.
     *
     * @param documentDbFactory the document db factory
     * @param properties        the properties
     * @return the document db template
     */
    public DocumentDbTemplate createDocumentDbTemplate(final DocumentDbFactory documentDbFactory,
                                                       final BaseCosmosDbProperties properties) {
        final DocumentDbMappingContext documentDbMappingContext = createDocumentDbMappingContext();
        final MappingDocumentDbConverter mappingDocumentDbConverter = createMappingDocumentDbConverter(documentDbMappingContext);
        return new DocumentDbTemplate(documentDbFactory, mappingDocumentDbConverter, properties.getDatabase());
    }

    /**
     * Create document db mapping context.
     *
     * @return the document db mapping context
     */
    @SneakyThrows
    public DocumentDbMappingContext createDocumentDbMappingContext() {
        final DocumentDbMappingContext documentDbMappingContext = new DocumentDbMappingContext();
        documentDbMappingContext.setInitialEntitySet(new EntityScanner(applicationContext).scan(Persistent.class));
        return documentDbMappingContext;
    }

    /**
     * Create mapping document db converter.
     *
     * @param documentDbMappingContext the document db mapping context
     * @return the mapping document db converter
     */
    private MappingDocumentDbConverter createMappingDocumentDbConverter(final DocumentDbMappingContext documentDbMappingContext) {
        return new MappingDocumentDbConverter(documentDbMappingContext);
    }

    /**
     * Gets database link.
     *
     * @param databaseName the database name
     * @return the database link
     */
    public static String getDatabaseLink(final String databaseName) {
        return "dbs/" + databaseName;
    }

    /**
     * Gets collection link.
     *
     * @param databaseName   the database name
     * @param collectionName the collection name
     * @return the collection link
     */
    public static String getCollectionLink(final String databaseName, final String collectionName) {
        return getDatabaseLink(databaseName) + "/colls/" + collectionName;
    }

    /**
     * Gets document link.
     *
     * @param databaseName   the database name
     * @param collectionName the collection name
     * @param documentId     the document id
     * @return the document link
     */
    public static String getDocumentLink(final String databaseName, final String collectionName, final String documentId) {
        return getCollectionLink(databaseName, collectionName) + "/docs/" + documentId;
    }
}
