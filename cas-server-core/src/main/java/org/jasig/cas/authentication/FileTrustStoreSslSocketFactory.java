/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * The SSL socket factory that loads the SSL context from a custom
 * truststore file strictly used ssl handshakes for proxy authentication. 
 * @author Misagh Moayyed
 * @since 4.1
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
        this(trustStoreFile, trustStorePassword,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER,
                KeyStore.getDefaultType());
    }

    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     * Uses the {@link DoesNotTrustStrategy} trust strategy.
     *
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     * @param hostnameVerifier the hostname verifier
     * @param trustStoreType the trust store type
     */
    public FileTrustStoreSslSocketFactory(final File trustStoreFile, final String trustStorePassword,
                                          final X509HostnameVerifier hostnameVerifier, final String trustStoreType) {
        this(trustStoreFile, trustStorePassword, hostnameVerifier, trustStoreType, new DoesNotTrustStrategy());
    }


    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     * @param hostnameVerifier the hostname verifier
     * @param trustStoreType the trust store type
     * @param trustStrategy the trust strategy
     */
    public FileTrustStoreSslSocketFactory(final File trustStoreFile, final String trustStorePassword,
                                          final X509HostnameVerifier hostnameVerifier,
                                          final String trustStoreType,
                                          final TrustStrategy trustStrategy) {
        super(getTrustedSslContext(trustStoreFile, trustStorePassword, trustStoreType,
               trustStrategy), hostnameVerifier);
    }

    /**
     * Gets the trusted ssl context.
     *
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     * @param trustStoreType the trust store type
     * @param trustStrategy the trust strategy. Could use {@link org.apache.http.conn.ssl.TrustSelfSignedStrategy}
     * @return the trusted ssl context
     */
    private static SSLContext getTrustedSslContext(final File trustStoreFile, final String trustStorePassword,
                                            final String trustStoreType, final TrustStrategy trustStrategy) {
        KeyStore trustStore = null;
        FileInputStream instream = null;
        try {

            if (!trustStoreFile.exists() || !trustStoreFile.canRead()) {
                throw new FileNotFoundException("Truststore file cannot be located at " + trustStoreFile.getCanonicalPath());
            }
            trustStore = KeyStore.getInstance(trustStoreType);
            instream = new FileInputStream(trustStoreFile);
            trustStore.load(instream, trustStorePassword.toCharArray());

            final SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, trustStrategy)
                    .useTLS()
                    .build();


            LOGGER.debug("Loaded [{}] entries in truststore [{}] for proxy authentication", trustStore.size(),
                    trustStoreFile.getCanonicalFile());

            return sslcontext;

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(instream);
        }
    }

    private static class DoesNotTrustStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
            return false;
        }
    }

}
