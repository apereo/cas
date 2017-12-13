package org.apereo.cas.authentication;

import org.apache.http.ssl.SSLContexts;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
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
public class DefaultCasSslContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasSslContext.class);
    private static final String ALG_NAME_PKIX = "PKIX";

    private final SSLContext sslContext;

    public DefaultCasSslContext(final Resource trustStoreFile, final String trustStorePassword, final String trustStoreType) {
        try {

            final KeyStore casTrustStore = KeyStore.getInstance(trustStoreType);
            final char[] trustStorePasswordCharArray = trustStorePassword.toCharArray();

            try (InputStream casStream = trustStoreFile.getInputStream()) {
                casTrustStore.load(casStream, trustStorePasswordCharArray);
            }

            final String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            final X509KeyManager customKeyManager = getKeyManager(ALG_NAME_PKIX, casTrustStore, trustStorePasswordCharArray);
            final X509KeyManager jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);

            final String defaultTrustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            final Collection<X509TrustManager> customTrustManager = getTrustManager(ALG_NAME_PKIX, casTrustStore);
            final Collection<X509TrustManager> jvmTrustManagers = getTrustManager(defaultTrustAlgorithm, null);

            final KeyManager[] keyManagers = {
                new CompositeX509KeyManager(CollectionUtils.wrapList(jvmKeyManager, customKeyManager))
            };
            final List<X509TrustManager> allManagers = new ArrayList<>(customTrustManager);
            allManagers.addAll(jvmTrustManagers);
            final TrustManager[] trustManagers = new TrustManager[]{new CompositeX509TrustManager(allManagers)};

            this.sslContext = SSLContexts.custom().useProtocol("SSL").build();
            sslContext.init(keyManagers, trustManagers, null);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets the trusted ssl context.
     *
     * @return the trusted ssl context
     */
    public SSLContext getSslContext() {
        return this.sslContext;
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
        final KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
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
        final TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
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
            final List<String> aliases = new ArrayList<>();
            this.keyManagers.forEach(keyManager -> aliases.addAll(CollectionUtils.wrapList(keyManager.getClientAliases(keyType, issuers))));
            return aliases.toArray(new String[]{});
        }

        @Override
        public String[] getServerAliases(final String keyType, final Principal[] issuers) {
            final List<String> aliases = new ArrayList<>();
            this.keyManagers.forEach(keyManager -> aliases.addAll(CollectionUtils.wrapList(keyManager.getServerAliases(keyType, issuers))));
            return aliases.toArray(new String[]{});
        }
    }

    /**
     * Represents an ordered list of {@link X509TrustManager}s with additive trust. If any one of the
     * composed managers trusts a certificate chain, then it is trusted by the composite manager.
     */
    private static class CompositeX509TrustManager implements X509TrustManager {

        private static final Logger LOGGER = LoggerFactory.getLogger(CompositeX509TrustManager.class);

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
            final boolean trusted = this.trustManagers.stream().anyMatch(trustManager -> {
                try {
                    trustManager.checkClientTrusted(chain, authType);
                    return true;
                } catch (final CertificateException e) {
                    final String msg = "Unable to trust the client certificates [%s] for auth type [%s]: [%s]";
                    LOGGER.debug(String.format(msg, Arrays.stream(chain).map(Certificate::toString).collect(Collectors.toSet()),
                        authType, e.getMessage()), e);
                    return false;
                }
            });

            if (!trusted) {
                throw new CertificateException("None of the TrustManagers can trust this client certificate chain");
            }
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

            final boolean trusted = this.trustManagers.stream().anyMatch(trustManager -> {
                try {
                    trustManager.checkServerTrusted(chain, authType);
                    return true;
                } catch (final CertificateException e) {
                    final String msg = "Unable to trust the server certificates [%s] for auth type [%s]: [%s]";
                    LOGGER.debug(String.format(msg, Arrays.stream(chain).map(Certificate::toString).collect(Collectors.toSet()),
                        authType, e.getMessage()), e);
                    return false;
                }
            });
            if (!trusted) {
                throw new CertificateException("None of the TrustManagers trust this server certificate chain");
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            final List<X509Certificate> certificates = new ArrayList<>();
            this.trustManagers.forEach(trustManager -> certificates.addAll(CollectionUtils.wrapList(trustManager.getAcceptedIssuers())));
            return certificates.toArray(new X509Certificate[certificates.size()]);
        }
    }
}
