package org.apereo.cas.authentication;

import com.google.common.base.Throwables;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The SSL socket factory that loads the SSL context from a custom
 * truststore file strictly used ssl handshakes for proxy authentication.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class FileTrustStoreSslSocketFactory extends SSLConnectionSocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTrustStoreSslSocketFactory.class);

    private static final String ALG_NAME_PKIX = "PKIX";

    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     * Defaults to {@code TLSv1} and {@link SSLConnectionSocketFactory#BROWSER_COMPATIBLE_HOSTNAME_VERIFIER}
     * for the supported protocols and hostname verification.
     *
     * @param trustStoreFile     the trust store file
     * @param trustStorePassword the trust store password
     */
    public FileTrustStoreSslSocketFactory(final Resource trustStoreFile, final String trustStorePassword) {
        this(trustStoreFile, trustStorePassword, KeyStore.getDefaultType());
    }


    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     *
     * @param trustStoreFile     the trust store file
     * @param trustStorePassword the trust store password
     * @param trustStoreType     the trust store type
     */
    public FileTrustStoreSslSocketFactory(final Resource trustStoreFile,
                                          final String trustStorePassword,
                                          final String trustStoreType) {
        super(getTrustedSslContext(trustStoreFile, trustStorePassword, trustStoreType));
    }

    /**
     * Gets the trusted ssl context.
     *
     * @param trustStoreFile     the trust store file
     * @param trustStorePassword the trust store password
     * @param trustStoreType     the trust store type
     * @return the trusted ssl context
     */
    private static SSLContext getTrustedSslContext(final Resource trustStoreFile, final String trustStorePassword,
                                                   final String trustStoreType) {
        try {

            final KeyStore casTrustStore = KeyStore.getInstance(trustStoreType);
            final char[] trustStorePasswordCharArray = trustStorePassword.toCharArray();

            try (InputStream casStream = trustStoreFile.getInputStream()) {
                casTrustStore.load(casStream, trustStorePasswordCharArray);
            }

            final String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            final X509KeyManager customKeyManager = getKeyManager(ALG_NAME_PKIX, casTrustStore, trustStorePasswordCharArray);
            final X509KeyManager jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);
            final X509TrustManager customTrustManager = getTrustManager(ALG_NAME_PKIX, casTrustStore);
            final X509TrustManager jvmTrustManager = getTrustManager(defaultAlgorithm, null);

            final KeyManager[] keyManagers = {
                    new CompositeX509KeyManager(Arrays.asList(jvmKeyManager, customKeyManager))
            };
            final TrustManager[] trustManagers = {
                    new CompositeX509TrustManager(Arrays.asList(jvmTrustManager, customTrustManager))
            };

            final SSLContext context = SSLContexts.custom().useProtocol("SSL").build();
            context.init(keyManagers, trustManagers, null);
            return context;

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
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
    private static X509TrustManager getTrustManager(final String algorithm,
                                                    final KeyStore keystore) throws Exception {
        final TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        return (X509TrustManager) factory.getTrustManagers()[0];
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
            this.keyManagers.forEach(keyManager -> aliases.addAll(Arrays.asList(keyManager.getClientAliases(keyType, issuers))));
            return aliases.toArray(new String[]{});
        }

        @Override
        public String[] getServerAliases(final String keyType, final Principal[] issuers) {
            final List<String> aliases = new ArrayList<>();
            this.keyManagers.forEach(keyManager -> aliases.addAll(Arrays.asList(keyManager.getServerAliases(keyType, issuers))));
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
                throw new CertificateException("None of the TrustManagers can trust this certificate chain");
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
                throw new CertificateException("None of the TrustManagers trust this certificate chain");
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            final List<X509Certificate> certificates = new ArrayList<>();
            this.trustManagers.forEach(trustManager -> certificates.addAll(Arrays.asList(trustManager.getAcceptedIssuers())));
            return certificates.toArray(new X509Certificate[certificates.size()]);
        }
    }
}
