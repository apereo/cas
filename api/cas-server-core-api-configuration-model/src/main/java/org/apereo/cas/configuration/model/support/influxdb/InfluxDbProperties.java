package org.apereo.cas.configuration.model.support.influxdb;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link InfluxDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class InfluxDbProperties implements Serializable {

    private static final long serialVersionUID = -1945287308473842616L;

    /**
     * InfluxDb connection url.
     */
    @RequiredProperty
    private String url = "http://localhost:8086";

    /**
     * InfluxDb connection username.
     */
    @RequiredProperty
    private String username = "root";

    /**
     * InfluxDb connection password.
     */
    @RequiredProperty
    private String password = "root";

    /**
     * Database name.
     */
    @RequiredProperty
    private String database;

    /**
     * Database retention policy to use.
     */
    private String retentionPolicy = "autogen";

    /**
     * Whether the indicated database should be dropped
     * and recreated.
     */
    private boolean dropDatabase;

    /**
     * The number of point to flush and write to the database
     * as part of the batch.
     */
    private int pointsToFlush = 100;

    /**
     * The interval used to run batch jobs
     * to flush points.
     */
    private String batchInterval = "PT5S";

    /**
     * Database consistency level.
     * Acceptable values are {@code ALL, ANY, ONE, QUORUM}.
     * <ul>
     * <li>ALL - Write succeeds only if write reached all cluster members.</li>
     * <li>ANY - Write succeeds if write reached any cluster members.</li>
     * <li>ONE - Write succeeds if write reached at least one cluster members.</li>
     * <li>QUORUM - Write succeeds only if write reached a quorum of cluster members.</li>
     * </ul>
     */
    private String consistencyLevel = "ALL";
}
