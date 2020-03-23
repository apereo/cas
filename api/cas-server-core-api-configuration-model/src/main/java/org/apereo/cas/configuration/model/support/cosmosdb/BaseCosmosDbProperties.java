package org.apereo.cas.configuration.model.support.cosmosdb;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseCosmosDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-core")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseCosmosDbProperties implements Serializable {

    private static final long serialVersionUID = 2528153816791719898L;

    /**
     * Database throughput usually between 400 or 100,000.
     */
    private int throughput = 10_000;

    /**
     * Document Db host address (i.e. https://localhost:8081).
     */
    @RequiredProperty
    private String uri;

    /**
     * Document Db master key.
     */
    @RequiredProperty
    private String key;

    /**
     * Document Db consistency level.
     * Azure Cosmos DB is designed from the ground up with global distribution in mind for every data model. It is designed to offer predictable
     * low latency guarantees, a 99.99% availability SLA, and multiple well-defined relaxed consistency models. Currently,
     * Azure Cosmos DB provides five consistency levels: strong, bounded-staleness, session, consistent prefix, and eventual.
     * Besides strong and eventual consistency models commonly offered by distributed databases, Azure Cosmos DB offers three more carefully
     * codified and operationalized consistency models, and has validated their usefulness against real world use cases. These are the
     * bounded staleness, session, and consistent prefix consistency levels. Collectively these
     * five consistency levels enable you to make well-reasoned trade-offs between consistency, availability, and latency.
     * Accepted values are:
     * <ul>
     * <li>{@code Strong}: Linearizability</li>
     * <li>{@code Session}: Consistent Prefix. Monotonic reads, monotonic writes, read-your-writes, write-follows-reads</li>
     * <li>{@code Eventual}: Out of order reads</li>
     * <li>{@code BoundedStaleness}: Consistent Prefix. Reads lag behind writes by k prefixes or t interval</li>
     * <li>{@code ConsistentPrefix}: Updates returned are some prefix of all the updates, with no gaps</li>
     * </ul>
     */
    private String consistencyLevel = "Session";

    /**
     * Database name.
     */
    @RequiredProperty
    private String database;

    /**
     * Whether telemetry should be enabled by default.
     */
    private boolean allowTelemetry = true;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    /**
     * Specifies the supported indexing modes in the Azure Cosmos DB database service.
     * Accepted values are:
     * <ul>
     * <li>{@code Consistent}: Index is updated synchronously with a create or update operation. With consistent indexing,
     * query behavior is the same as the default consistency level for the collection. The index is always kept up to date with the data.</li>
     * <li>{@code Lazy}: Index is updated asynchronously with respect to a create or update operation.
     * With lazy indexing, queries are eventually consistent. The index is updated when the collection is idle.
     * </li>
     * <li>{@code None}: No index is provided.
     * Setting IndexingMode to "None" drops the index. Use this if you don't want to maintain the index for a document
     * collection, to save the storage cost or improve the write throughput. Your queries will degenerate to scans of
     * the entire collection.
     * </li>
     * </ul>
     */
    private String indexingMode = "None";
}
