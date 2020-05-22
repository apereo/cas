package org.apereo.cas.couchdb.core;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.ObjectMapperFactory;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

/**
 * This is {@link CouchDbConnectorFactory}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class CouchDbConnectorFactory {

    private final @NonNull BaseCouchDbProperties couchDbProperties;

    private final @NonNull ObjectMapperFactory objectMapperFactory;

    @Getter(lazy = true)
    private final CouchDbConnector couchDbConnector = createConnector();

    @Getter(lazy = true)
    private final CouchDbInstance couchDbInstance = createInstance();

    @Getter(lazy = true)
    private final HttpClient httpClient = createHttpClient();

    /**
     * Create {@link CouchDbConnector} instance.
     *
     * @return CouchDbConnector instance from db properties.
     */
    public CouchDbConnector createConnector() {
        val connector = new StdCouchDbConnector(couchDbProperties.getDbName(), getCouchDbInstance(), objectMapperFactory);
        LOGGER.debug("Connector created: [{}]", connector);

        return connector;
    }

    /**
     * Create {@link CouchDbInstance} instance.
     *
     * @return CouchDbInstance instance from db properties.
     */
    public CouchDbInstance createInstance() {
        val instance = new StdCouchDbInstance(getHttpClient());
        LOGGER.debug("Instance created: [{}]", instance);
        return instance;
    }

    /**
     * Create {@link HttpClient} instance.
     *
     * @return HttpClient instance from db properties.
     */
    @SneakyThrows
    public HttpClient createHttpClient() {
        val builder = new StdHttpClient.Builder()
            .url(couchDbProperties.getUrl())
            .maxConnections(couchDbProperties.getMaxConnections())
            .maxCacheEntries(couchDbProperties.getMaxCacheEntries())
            .connectionTimeout(couchDbProperties.getConnectionTimeout())
            .socketTimeout(couchDbProperties.getSocketTimeout())
            .enableSSL(couchDbProperties.isEnableSsl())
            .relaxedSSLSettings(couchDbProperties.isRelaxedSslSettings())
            .caching(couchDbProperties.isCaching())
            .maxObjectSizeBytes(couchDbProperties.getMaxObjectSizeBytes())
            .useExpectContinue(couchDbProperties.isUseExpectContinue())
            .cleanupIdleConnections(couchDbProperties.isCleanupIdleConnections());


        if (StringUtils.isNotBlank(couchDbProperties.getProxyHost())) {
            builder.proxy(couchDbProperties.getProxyHost());

            if (couchDbProperties.getProxyPort() > 0) {
                builder.proxyPort(couchDbProperties.getProxyPort());
                LOGGER.info("CouchDb proxy settings enabled [{}]:[{}]", couchDbProperties.getProxyHost(), couchDbProperties.getProxyPort());
            } else {
                LOGGER.debug("Proxy port not set for host [{}] clearing proxy host.", couchDbProperties.getProxyHost());
                builder.proxy(null);
            }
        }

        if (StringUtils.isNotBlank(couchDbProperties.getUsername())) {
            builder.username(couchDbProperties.getUsername());
        }

        if (StringUtils.isNotBlank(couchDbProperties.getPassword())) {
            builder.password(couchDbProperties.getPassword());
        }

        val client = builder.build();
        LOGGER.debug("Client created: [{}]", client);
        return client;
    }
}
