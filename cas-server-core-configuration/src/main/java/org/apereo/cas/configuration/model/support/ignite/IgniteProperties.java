package org.apereo.cas.configuration.model.support.ignite;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link IgniteProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class IgniteProperties {
    private String igniteAddresses = "localhost:47500";
    private TicketsCache ticketsCache = new TicketsCache();
    
    private String keyStoreType;
    private String keyStoreFilePath;
    private String keyStorePassword;
    private String trustStoreType;
    private String protocol;
    private String keyAlgorithm;
    private String trustStoreFilePath;
    private String trustStorePassword;

    @NestedConfigurationProperty
    private CryptographyProperties crypto = new CryptographyProperties();

    public CryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final CryptographyProperties crypto) {
        this.crypto = crypto;
    }
    
    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(final String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStoreFilePath() {
        return keyStoreFilePath;
    }

    public void setKeyStoreFilePath(final String keyStoreFilePath) {
        this.keyStoreFilePath = keyStoreFilePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(final String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(final String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getTrustStoreFilePath() {
        return trustStoreFilePath;
    }

    public void setTrustStoreFilePath(final String trustStoreFilePath) {
        this.trustStoreFilePath = trustStoreFilePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getIgniteAddresses() {
        return igniteAddresses;
    }

    public void setIgniteAddresses(final String igniteAddresses) {
        this.igniteAddresses = igniteAddresses;
    }

    public TicketsCache getTicketsCache() {
        return ticketsCache;
    }

    public void setTicketsCache(final TicketsCache ticketsCache) {
        this.ticketsCache = ticketsCache;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("igniteAddresses", igniteAddresses)
                .append("ticketsCache", ticketsCache)
                .append("keyStoreType", keyStoreType)
                .append("keyStoreFilePath", keyStoreFilePath)
                .append("trustStoreType", trustStoreType)
                .append("protocol", protocol)
                .append("keyAlgorithm", keyAlgorithm)
                .append("trustStoreFilePath", trustStoreFilePath)
                .toString();
    }

    public static class TicketsCache {
        private String cacheName = "TicketsCache";
        private String cacheMode = "REPLICATED";
        private String atomicityMode = "TRANSACTIONAL";
        private String writeSynchronizationMode = "FULL_SYNC";

        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(final String cacheName) {
            this.cacheName = cacheName;
        }

        public String getCacheMode() {
            return cacheMode;
        }

        public void setCacheMode(final String cacheMode) {
            this.cacheMode = cacheMode;
        }

        public String getAtomicityMode() {
            return atomicityMode;
        }

        public void setAtomicityMode(final String atomicityMode) {
            this.atomicityMode = atomicityMode;
        }

        public String getWriteSynchronizationMode() {
            return writeSynchronizationMode;
        }

        public void setWriteSynchronizationMode(final String writeSynchronizationMode) {
            this.writeSynchronizationMode = writeSynchronizationMode;
        }
    }
    
    
}


