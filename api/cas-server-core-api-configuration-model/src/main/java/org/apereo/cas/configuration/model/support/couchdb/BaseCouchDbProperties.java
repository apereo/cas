package org.apereo.cas.configuration.model.support.couchdb;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseCouchDbProperties}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-couchdb-core")
@Accessors(chain = true)
public abstract class BaseCouchDbProperties implements Serializable {

    private static final long serialVersionUID = 1323894615409106853L;

    /**
     * Connection url.
     */
    private String url = "http://localhost:5984";

    /**
     * Username for connection.
     */
    private String username;
    /**
     * Password for connection.
     */
    private String password;

    /**
     * Socket idle timeout.
     */
    private int socketTimeout = 10000;

    /**
     * TCP connection timeout.
     */
    private int connectionTimeout = 1000;

    /**
     * Maximum connections to CouchDB.
     */
    private int maxConnections = 20;

    /**
     * Use TLS. Only needed if not specified by URL.
     */
    private boolean enableSsl;

    /**
     * Relax TLS settings–like certificate verification.
     */
    private boolean relaxedSslSettings;

    /**
     * Use a local cache to reduce fetches..
     */
    private boolean caching = true;

    /**
     * Max entries in local cache.
     */
    private int maxCacheEntries = 1000;

    /**
     * Largest allowable serialized object.
     */
    private int maxObjectSizeBytes = 8192;

    /**
     * Expect HTTP 100 Continue during connection.
     */
    private boolean useExpectContinue = true;

    /**
     * Remove idle connections from pool.
     */
    private boolean cleanupIdleConnections = true;

    /**
     * Create the database if it doesn't exist.
     */
    private boolean createIfNotExists = true;

    /**
     * Retries for update conflicts.
     */
    private int retries = 5;

    /**
     * Database name. Defaults to +serviceRegistry+ and +ticketRegistry+ for the service and ticket registries,
     * respectively.
     */
    private String dbName;

    /**
     * Proxy host.
     */
    private String proxyHost;

    /**
     * proxy port.
     */
    private int proxyPort = -1;
}
