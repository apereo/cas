/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The SSL socket factory that loads the SSL context from a custom
 * truststore file strictly used ssl handshakes for proxy authentication. 
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class FileTrustStoreSslSocketFactory extends SSLConnectionSocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTrustStoreSslSocketFactory.class);

    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     * Defaults to <code>TLSv1</code> and {@link SSLConnectionSocketFactory#BROWSER_COMPATIBLE_HOSTNAME_VERIFIER}
     * for the supported protocols and hostname verification.
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     */
    public FileTrustStoreSslSocketFactory(final File trustStoreFile, final String trustStorePassword) {
        this(trustStoreFile, trustStorePassword, KeyStore.getDefaultType());
    }


    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     * @param trustStoreType the trust store type
     */
    public FileTrustStoreSslSocketFactory(final File trustStoreFile, final String trustStorePassword,
                                          final String trustStoreType) {
        super(getTrustedSslContext(trustStoreFile, trustStorePassword, trustStoreType));
    }

    /**
     * Gets the trusted ssl context.
     *
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     * @param trustStoreType the trust store type
     * @return the trusted ssl context
     */
    private static SSLContext getTrustedSslContext(final File trustStoreFile, final String trustStorePassword,
                                            final String trustStoreType) {
        try {

            if (!trustStoreFile.exists() || !trustStoreFile.canRead()) {
                throw new FileNotFoundException("Truststore file cannot be located at " + trustStoreFile.getCanonicalPath());
            }

            final KeyStore casTrustStore = KeyStore.getInstance(trustStoreType);
            final char[] trustStorePasswordCharArray = trustStorePassword.toCharArray();

            try (FileInputStream casStream = new FileInputStream(trustStoreFile)) {
                casTrustStore.load(casStream, trustStorePasswordCharArray);
            }

            final String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            final X509KeyManager customKeyManager = getKeyManager("PKIX", casTrustStore, trustStorePasswordCharArray);
            final X509KeyManager jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);
            final X509TrustManager customTrustManager = getTrustManager("PKIX", casTrustStore);
            final X509TrustManager jvmTrustManager = getTrustManager(defaultAlgorithm, null);

            final KeyManager[] keyManagers = {
                    new CompositeX509KeyManager(Arrays.asList(jvmKeyManager, customKeyManager))
            };
            final TrustManager[] trustManagers = {
                    new CompositeX509TrustManager(Arrays.asList(jvmTrustManager, customTrustManager))
            };

            final SSLContext context = SSLContexts.custom().useSSL().build();
            context.init(keyManagers, trustManagers, null);
            return context;

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets key manager.
     *
     * @param algorithm the algorithm
     * @param keystore the keystore
     * @param password the password
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
     * @param keystore the keystore
     * @return the trust manager
     * @throws Exception the exception
     */
    private static X509TrustManager getTrustManager(final String algorithm,
                                                    final KeyStore keystore) throws Exception{
        final TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        return (X509TrustManager) factory.getTrustManagers()[0];
    }

    private static class DoesNotTrustStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
            return false;
        }
    }

    private static class CompositeX509KeyManager implements X509KeyManager {

        private final List<X509KeyManager> keyManagers;

        /**
         * Represents an ordered list of {@link X509KeyManager}s with most-preferred managers first.
         * @param keyManagers list of key managers
         */
        CompositeX509KeyManager(final List<X509KeyManager> keyManagers) {
            this.keyManagers = keyManagers;
        }

        @Override
        public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
            for (final X509KeyManager keyManager : keyManagers) {
                final String alias = keyManager.chooseClientAlias(keyType, issuers, socket);
                if (alias != null) {
                    return alias;
                }
            }
            return null;
        }


        @Override
        public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
            for (final X509KeyManager keyManager : keyManagers) {
                final String alias = keyManager.chooseServerAlias(keyType, issuers, socket);
                if (alias != null) {
                    return alias;
                }
            }
            return null;
        }


        @Override
        public PrivateKey getPrivateKey(final String alias) {
            for (final X509KeyManager keyManager : keyManagers) {
                final PrivateKey privateKey = keyManager.getPrivateKey(alias);
                if (privateKey != null) {
                    return privateKey;
                }
            }
            return null;
        }


        @Override
        public X509Certificate[] getCertificateChain(final String alias) {
            for (final X509KeyManager keyManager : keyManagers) {
                final X509Certificate[] chain = keyManager.getCertificateChain(alias);
                if (chain != null && chain.length > 0) {
                    return chain;
                }
            }
            return null;
        }

        @Override
        public String[] getClientAliases(final String keyType, final Principal[] issuers) {
            final List<String> aliases = new ArrayList<>();
            for (final X509KeyManager keyManager : keyManagers) {
                final List<String> list = Arrays.asList(keyManager.getClientAliases(keyType, issuers));
                aliases.addAll(list);
            }
            return aliases.toArray(new String[] {});
        }

        @Override
        public  String[] getServerAliases(final String keyType, final Principal[] issuers) {
            final List<String> aliases = new ArrayList<>();
            for (final X509KeyManager keyManager : keyManagers) {
                final List<String> list = Arrays.asList(keyManager.getServerAliases(keyType, issuers));
                aliases.addAll(list);
            }
            return aliases.toArray(new String[] {});
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
            for (final X509TrustManager trustManager : trustManagers) {
                try {
                    trustManager.checkClientTrusted(chain, authType);
                    return;
                } catch (final CertificateException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
            throw new CertificateException("None of the TrustManagers trust this certificate chain");
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            for (final X509TrustManager trustManager : trustManagers) {
                try {
                    trustManager.checkServerTrusted(chain, authType);
                    return;
                } catch (final CertificateException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
            throw new CertificateException("None of the TrustManagers trust this certificate chain");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            final List<X509Certificate> certificates = new ArrayList<>();
            for (final X509TrustManager trustManager : trustManagers) {
                final List<X509Certificate> list = Arrays.asList(trustManager.getAcceptedIssuers());
                certificates.addAll(list);
            }
            return certificates.toArray(new X509Certificate[] {});
        }
    }
}
