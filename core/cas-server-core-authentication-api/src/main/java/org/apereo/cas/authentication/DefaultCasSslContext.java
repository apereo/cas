package org.apereo.cas.authentication;

import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.ssl.SSLContexts;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasSslContext}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public class DefaultCasSslContext {
    private static final String ALG_NAME_PKIX = "PKIX";

    private final SSLContext sslContext;

    public DefaultCasSslContext(final Resource trustStoreFile, final String trustStorePassword, final String trustStoreType) throws IOException {
        try {
            this.sslContext = initialize(trustStoreFile, trustStorePassword, trustStoreType);
        } catch (final Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static SSLContext initialize(final Resource trustStoreFile, final String trustStorePassword, final String trustStoreType) throws Exception {
        val casTrustStore = KeyStore.getInstance(trustStoreType);
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

        val keyManagers = new KeyManager[] {
            new CompositeX509KeyManager(CollectionUtils.wrapList(jvmKeyManager, customKeyManager))
        };
        val allManagers = new ArrayList<X509TrustManager>(customTrustManager);
        allManagers.addAll(jvmTrustManagers);
        val trustManagers = new TrustManager[]{new CompositeX509TrustManager(allManagers)};

        val sslContext = SSLContexts.custom().setProtocol("SSL").build();
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }

    /**
     * Gets key manager.
     *
     * @param algorithm the algorithm
     * @param keystore  the keystore
     * @param password  the password
     * @return the key manager
     * @throws Exception the exception
     */
    private static X509KeyManager getKeyManager(final String algorithm, final KeyStore keystore,
                                                final char[] password) throws Exception {
        val factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keystore, password);
        return (X509KeyManager) factory.getKeyManagers()[0];
    }

    /**
     * Gets trust manager.
     *
     * @param algorithm the algorithm
     * @param keystore  the keystore
     * @return the trust manager
     * @throws Exception the exception
     */
    private static Collection<X509TrustManager> getTrustManager(final String algorithm, final KeyStore keystore) throws Exception {
        val factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        return Arrays.stream(factory.getTrustManagers())
            .filter(e -> e instanceof X509TrustManager)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());
    }

    private static class CompositeX509KeyManager implements X509KeyManager {

        private final List<X509KeyManager> keyManagers;

        /**
         * Represents an ordered list of {@link X509KeyManager}s with most-preferred managers first.
         *
         * @param keyManagers list of key managers
         */
        CompositeX509KeyManager(final List<X509KeyManager> keyManagers) {
            this.keyManagers = keyManagers;
        }

        @Override
        public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
            return this.keyManagers.stream().map(keyManager -> keyManager.chooseClientAlias(keyType, issuers, socket))
                .filter(Objects::nonNull).findFirst().orElse(null);
        }


        @Override
        public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
            return this.keyManagers.stream().map(keyManager -> keyManager.chooseServerAlias(keyType, issuers, socket))
                .filter(Objects::nonNull).findFirst().orElse(null);
        }


        @Override
        public PrivateKey getPrivateKey(final String alias) {
            return this.keyManagers.stream().map(keyManager -> keyManager.getPrivateKey(alias))
                .filter(Objects::nonNull).findFirst().orElse(null);
        }


        @Override
        public X509Certificate[] getCertificateChain(final String alias) {
            return this.keyManagers.stream().map(keyManager -> keyManager.getCertificateChain(alias))
                .filter(chain -> chain != null && chain.length > 0)
                .findFirst().orElse(null);
        }

        @Override
        public String[] getClientAliases(final String keyType, final Principal[] issuers) {
            val aliases = new ArrayList<String>(keyManagers.size());
            this.keyManagers.forEach(keyManager -> aliases.addAll(CollectionUtils.wrapList(keyManager.getClientAliases(keyType, issuers))));
            return aliases.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        }

        @Override
        public String[] getServerAliases(final String keyType, final Principal[] issuers) {
            val aliases = new ArrayList<String>(keyManagers.size());
            this.keyManagers.forEach(keyManager -> aliases.addAll(CollectionUtils.wrapList(keyManager.getServerAliases(keyType, issuers))));
            return aliases.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        }
    }

    /**
     * Represents an ordered list of {@link X509TrustManager}s with additive trust. If any one of the
     * composed managers trusts a certificate chain, then it is trusted by the composite manager.
     */
    @Slf4j
    private static class CompositeX509TrustManager implements X509TrustManager {


        private final List<X509TrustManager> trustManagers;

        /**
         * Instantiates a new Composite x 509 trust manager.
         *
         * @param trustManagers the trust managers
         */
        CompositeX509TrustManager(final List<X509TrustManager> trustManagers) {
            this.trustManagers = trustManagers;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            val trusted = this.trustManagers.stream().anyMatch(trustManager -> {
                try {
                    trustManager.checkClientTrusted(chain, authType);
                    return true;
                } catch (final CertificateException e) {
                    val msg = "Unable to trust the client certificates [%s] for auth type [%s]: [%s]";
                    LOGGER.debug(String.format(msg, Arrays.stream(chain).map(Certificate::toString).collect(Collectors.toSet()), authType, e.getMessage()), e);
                    return false;
                }
            });

            if (!trusted) {
                throw new CertificateException("None of the TrustManagers can trust this client certificate chain");
            }
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

            val trusted = this.trustManagers.stream().anyMatch(trustManager -> {
                try {
                    trustManager.checkServerTrusted(chain, authType);
                    return true;
                } catch (final CertificateException e) {
                    val msg = "Unable to trust the server certificates [%s] for auth type [%s]: [%s]";
                    LOGGER.debug(String.format(msg, Arrays.stream(chain).map(Certificate::toString).collect(Collectors.toSet()), authType, e.getMessage()), e);
                    return false;
                }
            });
            if (!trusted) {
                throw new CertificateException("None of the TrustManagers trust this server certificate chain");
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            val certificates = new ArrayList<X509Certificate>(trustManagers.size());
            this.trustManagers.forEach(trustManager -> certificates.addAll(CollectionUtils.wrapList(trustManager.getAcceptedIssuers())));
            return certificates.toArray(X509Certificate[]::new);
        }
    }
}
