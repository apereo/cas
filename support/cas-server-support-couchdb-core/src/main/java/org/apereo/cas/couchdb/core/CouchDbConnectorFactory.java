package org.apereo.cas.couchdb.core;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.couchdb.AbstractCouchDbProperties;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * This is {@link CouchDbConnectorFactory}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class CouchDbConnectorFactory {

    private final AbstractCouchDbProperties couchDbProperties;
    private CouchDbConnector couchDbConnector;
    private CouchDbInstance couchDbInstance;

    /**
     * Create {@link CouchDbConnector} instance.
     * @return CouchDbConnector instance from db properties.
     */
    @SneakyThrows
    public CouchDbConnector create() {
        final var builder = new StdHttpClient.Builder()
            .url(couchDbProperties.getUrl())
            .maxConnections(couchDbProperties.getMaxConnections())
            .maxCacheEntries(couchDbProperties.getMaxCacheEntries())
            .connectionTimeout(couchDbProperties.getConnectionTimeout())
            .socketTimeout(couchDbProperties.getSocketTimeout())
            .enableSSL(couchDbProperties.isEnableSSL())
            .relaxedSSLSettings(couchDbProperties.isRelaxedSSLSettings())
            .caching(couchDbProperties.isCaching())
            .maxObjectSizeBytes(couchDbProperties.getMaxObjectSizeBytes())
            .useExpectContinue(couchDbProperties.isUseExpectContinue())
            .cleanupIdleConnections(couchDbProperties.isCleanupIdleConnections());


        if (!StringUtils.isBlank(couchDbProperties.getUsername())) {
            builder.username(couchDbProperties.getUsername());
        }

        if (!StringUtils.isBlank(couchDbProperties.getPassword())) {
            builder.password(couchDbProperties.getPassword());
        }

        final var httpClient = builder.build();
        couchDbInstance = new StdCouchDbInstance(httpClient);
        couchDbConnector = new StdCouchDbConnector(couchDbProperties.getDbName(), couchDbInstance);
        return couchDbConnector;
    }
}
