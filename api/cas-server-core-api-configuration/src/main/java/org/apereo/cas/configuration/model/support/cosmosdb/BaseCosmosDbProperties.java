package org.apereo.cas.configuration.model.support.cosmosdb;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BaseCosmosDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-core")
@Slf4j
@Getter
@Setter
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
}
