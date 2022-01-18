package org.apereo.cas.configuration.model.support.ehcache;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link Ehcache3TerracottaProperties}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-ehcache3-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Ehcache3TerracottaProperties")
public class Ehcache3TerracottaProperties implements Serializable {

    private static final long serialVersionUID = 1112510035918976450L;

    /**
     * URI in format something like:
     * {@code terracotta://host1.company.org:9410,host2.company.org:9410/cas-application}.
     * Default port for terracotta (9410) is used if not specified in URI.
     */
    private String terracottaClusterUri;

    /**
     * Name of default server resource on Terracotta cluster.
     */
    private String defaultServerResource = "main";

    /**
     * Name of resource pool to use on Terracotta cluster.
     */
    private String resourcePoolName = "cas-ticket-pool";

    /**
     * Size of resource pool on terracotta cluster.
     */
    private String resourcePoolSize = "15MB";

    /**
     * Timeout when reading or writing to/from Terracotta cluster.
     */
    private long clusterReadWriteTimeout = 5L;

    /**
     * Timeout when connecting to Terracotta cluster.
     */
    private long clusterConnectionTimeout = 150L;

    /**
     * Determine the cluster consistency.
     */
    private Consistency clusteredCacheConsistency = Consistency.STRONG;

    /**
     * Enumeration of the different consistency levels supported in clustered caches.
     */
    public enum Consistency {
        /**
         * Indicates that the visibility of mutative operations is not guaranteed on operation completion.
         */
        EVENTUAL,
        /**
         * Indicates that the visibility of mutative operations is guaranteed on operation completion.
         */
        STRONG
    }
}

