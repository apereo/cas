package org.apereo.cas.configuration.model.support.cosmosdb;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
@JsonFilter("BaseCosmosDbProperties")
public abstract class BaseCosmosDbProperties implements Serializable {

    private static final long serialVersionUID = 2528153816791719898L;

    /**
     * Document Db host address (i.e. https://localhost:8081).
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String uri;

    /**
     * Document Db master key.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
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
     * <li>{@code STRONG}: Linearizability</li>
     * <li>{@code SESSION}: Consistent Prefix. Monotonic reads, monotonic writes, read-your-writes, write-follows-reads</li>
     * <li>{@code EVENTUAL}: Out of order reads</li>
     * <li>{@code BOUNDED_STALENESS}: Consistent Prefix. Reads lag behind writes by k prefixes or t interval</li>
     * <li>{@code CONSISTENT_PREFIX}: Updates returned are some prefix of all the updates, with no gaps</li>
     * </ul>
     */
    private String consistencyLevel = "SESSION";

    /**
     * Sets the flag to enable endpoint discovery for geo-replicated database accounts.
     * When EnableEndpointDiscovery is true, the SDK will automatically discover
     * the current write and read regions to ensure requests are sent to the correct region
     * based on the capability of the region and the user's preference.
     */
    private boolean endpointDiscoveryEnabled = true;

    /**
     * Database name.
     */
    @RequiredProperty
    private String database;

    /**
     * The max auto scale throughput.
     */
    private int databaseThroughput = 4000;

    /**
     * Whether telemetry should be enabled by default.
     * Sets the flag to enable client telemetry which will periodically collect
     * database operations aggregation statistics, system information like cpu/memory and send
     * it to cosmos monitoring service, which will be helpful during debugging.
     */
    private boolean allowTelemetry;

    /**
     * Whether collections should be created on startup.
     */
    private boolean createContainer;

    /**
     * Sets the preferred regions for geo-replicated database accounts. For example, "East US" as the preferred region.
     * When EnableEndpointDiscovery is true and PreferredRegions is
     * non-empty, the SDK will prefer to use the regions in the container in the
     * order they are specified to perform operations.
     */
    private List<String> preferredRegions = new ArrayList<>();

    /**
     * Sets the value of the user-agent suffix.
     */
    private String userAgentSuffix;

    /**
     * Sets the maximum number of retries in the case where the request fails
     * because the service has applied rate limiting on the client.
     * <p>
     * When a client is sending requests faster than the allowed rate, the service
     * will return HttpStatusCode 429 (Too Many Request) to throttle the client. The
     * current implementation in the SDK will then wait for the amount of time the
     * service tells it to wait and retry after the time has elapsed.
     * <p>
     * The default value is 4. This means in the case where the request is throttled,
     * the same request will be issued for a maximum of 5 times to the server
     * before an error is returned to the application.
     */
    private int maxRetryAttemptsOnThrottledRequests = 4;

    /**
     * Sets the maximum retry time in seconds.
     * When a request fails due to a throttle error, the service sends back a
     * response that contains a value indicating the client should not retry before the
     * time period has elapsed (Retry-After). The MaxRetryWaitTime flag allows the
     * application to set a maximum wait time for all retry attempts. If the cumulative
     * wait time exceeds the MaxRetryWaitTime, the SDK will stop retrying and return
     * the error to the application.
     */
    @DurationCapable
    private String maxRetryWaitTime = "PT10S";

    /**
     * Specifies the supported indexing modes in the Azure Cosmos DB database service.
     * Accepted values are:
     * <ul>
     * <li>{@code CONSISTENT}: Index is updated synchronously with a create or update operation. With consistent indexing,
     * query behavior is the same as the default consistency level for the collection. The index is always kept up to date with the data.</li>
     * <li>{@code LAZY}: Index is updated asynchronously with respect to a create or update operation.
     * With lazy indexing, queries are eventually consistent. The index is updated when the collection is idle.
     * </li>
     * <li>{@code NONE}: No index is provided.
     * Setting IndexingMode to "None" drops the index. Use this if you don't want to maintain the index for a document
     * collection, to save the storage cost or improve the write throughput. Your queries will degenerate to scans of
     * the entire collection.
     * </li>
     * </ul>
     */
    private String indexingMode = "NONE";
}
