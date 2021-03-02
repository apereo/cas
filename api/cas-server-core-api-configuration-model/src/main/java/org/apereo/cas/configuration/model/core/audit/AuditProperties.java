package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.dynamodb.AuditDynamoDbProperties;
import org.apereo.cas.configuration.model.support.redis.AuditRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link AuditProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-audit", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AuditProperties")
public class AuditProperties implements Serializable {

    private static final long serialVersionUID = 3946106584608417663L;

    /**
     * Core auditing engine functionality and settings
     * are captured here, separate from audit storage services.
     */
    @NestedConfigurationProperty
    private AuditEngineProperties engine = new AuditEngineProperties();

    /**
     * Family of sub-properties pertaining to Jdbc-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditJdbcProperties jdbc = new AuditJdbcProperties();

    /**
     * Family of sub-properties pertaining to MongoDb-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditMongoDbProperties mongo = new AuditMongoDbProperties();

    /**
     * Family of sub-properties pertaining to CouchDb-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditCouchDbProperties couchDb = new AuditCouchDbProperties();

    /**
     * Family of sub-properties pertaining to Redis-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditRedisProperties redis = new AuditRedisProperties();

    /**
     * Family of sub-properties pertaining to rest-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditRestProperties rest = new AuditRestProperties();

    /**
     * Family of sub-properties pertaining to file-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditSlf4jLogProperties slf4j = new AuditSlf4jLogProperties();

    /**
     * Family of sub-properties pertaining to couchbase-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditCouchbaseProperties couchbase = new AuditCouchbaseProperties();

    /**
     * Family of sub-properties pertaining to dynamodb-based audit destinations.
     */
    @NestedConfigurationProperty
    private AuditDynamoDbProperties dynamoDb = new AuditDynamoDbProperties();
}
