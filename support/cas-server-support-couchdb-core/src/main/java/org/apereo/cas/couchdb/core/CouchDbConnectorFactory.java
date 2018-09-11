package org.apereo.cas.couchdb.core;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Setter
@RequiredArgsConstructor
@Slf4j
public class CouchDbConnectorFactory {

    private final BaseCouchDbProperties couchDbProperties;
    private final ObjectMapperFactory objectMapperFactory;
    private CouchDbConnector couchDbConnector;
    private CouchDbInstance couchDbInstance;
    private HttpClient httpClient;

    /**
     * Create {@link CouchDbConnector} instance.
     *
     * @return CouchDbConnector instance from db properties.
     */
    public CouchDbConnector create() {
        return createConnector();
    }

    /**
     * Create {@link CouchDbConnector} instance.
     *
     * @return CouchDbConnector instance from db properties.
     */
    public CouchDbConnector createConnector() {
        if (couchDbConnector != null) {
            LOGGER.debug("Connector already initialized!");
            return couchDbConnector;
        }

        couchDbConnector = new StdCouchDbConnector(couchDbProperties.getDbName(), createInstance(), objectMapperFactory);
        LOGGER.debug("Connector created: [{}]", couchDbConnector);

        return couchDbConnector;
    }

    /**
     * Create {@link CouchDbInstance} instance.
     *
     * @return CouchDbInstance instance from db properties.
     */
    public CouchDbInstance createInstance() {
        if (couchDbInstance!= null) {
            LOGGER.debug("Instance already initialized!");
            return couchDbInstance;
        }
        couchDbInstance = new StdCouchDbInstance(createHttpClient());
        LOGGER.debug("Instance created: [{}]", couchDbInstance);
        return couchDbInstance;
    }

    /**
     * Create {@link HttpClient} instance.
     *
     * @return HttpClient instance from db properties.
     */
    @SneakyThrows
    public HttpClient createHttpClient() {
        if (httpClient != null) {
            LOGGER.debug("HTTP client already initialized!");
            return httpClient;
        }


        val builder = new StdHttpClient.Builder()
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


        if (StringUtils.isNotBlank(couchDbProperties.getProxyHost())) {
            builder.proxy(couchDbProperties.getProxyHost());

            if (couchDbProperties.getProxyPort() > 0) {
                builder.proxyPort(couchDbProperties.getProxyPort());
                LOGGER.info("CouchDb proxy settings enabled [{}:{}]", couchDbProperties.getProxyHost(), couchDbProperties.getProxyPort());
            } else {
                LOGGER.debug("Proxy port not set for host [{}] clearing proxy host.", couchDbProperties.getProxyHost());
                builder.proxy(null);
            }
        }

        if (StringUtils.isNotBlank(couchDbProperties.getPassword())) {
            builder.password(couchDbProperties.getPassword());
        }

        httpClient = builder.build();
        LOGGER.debug("Client created: [{}]", httpClient);
        return httpClient;
    }
}
