package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ssl.CompositeX509KeyManager;
import org.apereo.cas.util.ssl.CompositeX509TrustManager;

import lombok.Getter;
import lombok.val;
import org.apache.hc.core5.ssl.SSLContexts;
import org.jooq.lambda.Unchecked;
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

    private final KeyManagerFactory keyManagerFactory;

    public DefaultCasSSLContext(final Resource trustStoreFile,
                                final String trustStorePassword,
                                final String trustStoreType,
                                final HttpClientProperties httpClientProperties,
                                final HostnameVerifier hostnameVerifier) throws Exception {

        val disabled = "none".equalsIgnoreCase(httpClientProperties.getHostNameVerifier());
        if (disabled) {
            this.trustManagers = CasSSLContext.disabled().getTrustManagers();
            this.keyManagerFactory = CasSSLContext.disabled().getKeyManagerFactory();
            this.casTrustStore = null;
            this.keyManagers = CasSSLContext.disabled().getKeyManagers();
        } else {
            casTrustStore = KeyStore.getInstance(trustStoreType);
            val trustStorePasswordCharArray = trustStorePassword.toCharArray();
            try (val casStream = trustStoreFile.getInputStream()) {
                casTrustStore.load(casStream, trustStorePasswordCharArray);
            }

            this.keyManagerFactory = getKeyManagerFactory(ALG_NAME_PKIX, casTrustStore, trustStorePasswordCharArray);
            val customKeyManager = (X509KeyManager) keyManagerFactory.getKeyManagers()[0];

            val defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

            val jvmKeyManagerFactory = getKeyManagerFactory(defaultAlgorithm, null, null);
            val jvmKeyManager = (X509KeyManager) jvmKeyManagerFactory.getKeyManagers()[0];

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

    private static KeyManagerFactory getKeyManagerFactory(final String algorithm, final KeyStore keystore,
                                                          final char[] password) throws Exception {
        val factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keystore, password);
        return factory;
    }

    private static Collection<X509TrustManager> getTrustManager(final String algorithm,
                                                                final KeyStore keystore) throws Exception {
        val factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        return Arrays.stream(factory.getTrustManagers())
            .filter(X509TrustManager.class::isInstance)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());
    }

    @Override
    public TrustManagerFactory getTrustManagerFactory() {
        return Unchecked.supplier(() -> {
            val factory = TrustManagerFactory.getInstance(ALG_NAME_PKIX);
            factory.init(this.casTrustStore);
            return factory;
        }).get();
    }
}
