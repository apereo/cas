package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link DynamoDbDaxProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-core")
@Getter
@Setter
@Accessors(chain = true)
public class DynamoDbDaxProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 222540148774854955L;

    /**
     * Cluster url. For example, {@code dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com}.
     */
    @RequiredProperty
    private String url;

    /**
     * Connection timeout, calculated in milliseconds.
     */
    @DurationCapable
    private String connectTimeout = "PT5S";

    /**
     * How long should connections be kept alive, calculated in milliseconds.
     */
    @DurationCapable
    private String connectionTtl = "PT0S";

    /**
     * Connection idle timeout, calculated in milliseconds.
     */
    @DurationCapable
    private String idleTimeout = "PT15S";

    /**
     * Request execution timeout, calculated in milliseconds.
     */
    @DurationCapable
    private String requestTimeout = "PT5S";

    /**
     * Number of read retry attempts.
     */
    private int readRetries = 2;
    /**
     * Number of write retry attempts.
     */
    private int writeRetries = 2;

    /**
     * Determines the maximum number of concurrent requests that can be made to the DAX cluster.
     */
    private int maxConcurrency = 1000;
}
