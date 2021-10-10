package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ssl.CompositeX509KeyManager;
import org.apereo.cas.util.ssl.CompositeX509TrustManager;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.ssl.SSLContexts;
import org.springframework.core.io.Resource;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasSSLContext}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public class DefaultCasSSLContext implements CasSSLContext {
    private static final String ALG_NAME_PKIX = "PKIX";

    private final SSLContext sslContext;

    private final TrustManager[] trustManagers;

    private final KeyManager[] keyManagers;

    private final HostnameVerifier hostnameVerifier;

    private final KeyStore casTrustStore;

    public DefaultCasSSLContext(final Resource trustStoreFile,
                                final String trustStorePassword,
                                final String trustStoreType,
                                final HttpClientProperties httpClientProperties,
                                final HostnameVerifier hostnameVerifier) throws Exception {
        val disabled = httpClientProperties.getHostNameVerifier().equalsIgnoreCase("none");
        if (disabled) {
            this.trustManagers = CasSSLContext.disabled().getTrustManagers();
            this.casTrustStore = null;
            this.keyManagers = CasSSLContext.disabled().getKeyManagers();
        } else {
            casTrustStore = KeyStore.getInstance(trustStoreType);
            val trustStorePasswordCharArray = trustStorePassword.toCharArray();
            try (val casStream = trustStoreFile.getInputStream()) {
                casTrustStore.load(casStream, trustStorePasswordCharArray);
            }
            val defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            val customKeyManager = getKeyManager(ALG_NAME_PKIX, casTrustStore, trustStorePasswordCharArray);
            val jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);
            val defaultTrustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            val customTrustManager = getTrustManager(ALG_NAME_PKIX, casTrustStore);
            val jvmTrustManagers = getTrustManager(defaultTrustAlgorithm, null);
            val allManagers = new ArrayList<>(customTrustManager);
            allManagers.addAll(jvmTrustManagers);
            this.trustManagers = new TrustManager[]{new CompositeX509TrustManager(allManagers)};
            this.keyManagers = new KeyManager[]{
                new CompositeX509KeyManager(CollectionUtils.wrapList(jvmKeyManager, customKeyManager))
            };
        }
        this.sslContext = SSLContexts.custom().setProtocol("SSL").build();
        this.sslContext.init(this.keyManagers, this.trustManagers, null);
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    @SneakyThrows
    public TrustManagerFactory getTrustManagerFactory() {
        val factory = TrustManagerFactory.getInstance(ALG_NAME_PKIX);
        factory.init(this.casTrustStore);
        return factory;
    }

    @SneakyThrows
    private static X509KeyManager getKeyManager(final String algorithm, final KeyStore keystore, final char[] password) {
        val factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keystore, password);
        return (X509KeyManager) factory.getKeyManagers()[0];
    }
    
    @SneakyThrows
    private static Collection<X509TrustManager> getTrustManager(final String algorithm, final KeyStore keystore) {
        val factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        return Arrays.stream(factory.getTrustManagers())
            .filter(e -> e instanceof X509TrustManager)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());
    }
}
