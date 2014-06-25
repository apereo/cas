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
package org.jasig.cas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SSL socket factory that loads the SSL context from a custom
 * truststore file strictly used ssl handshakes for proxy authentication. 
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class TrustedProxyAuthenticationTrustStoreSSLSocketFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TrustedProxyAuthenticationTrustStoreSSLSocketFactory.class);
    
    private final String trustStoreType;
    private final File trustStoreFile;
    private final String trustStorePassword;
    private final List<String> supportedProtocols;
    private final X509HostnameVerifier hostnameVerifier;
    
    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     * Defaults to <code>TLSv1</code> and {@link SSLConnectionSocketFactory#BROWSER_COMPATIBLE_HOSTNAME_VERIFIER}
     * for the supported protocols and hostname verification.
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     */
    public TrustedProxyAuthenticationTrustStoreSSLSocketFactory(final File trustStoreFile, final String trustStorePassword) {
        this(trustStoreFile, trustStorePassword, Arrays.asList("TLSv1"),
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER,
                KeyStore.getDefaultType());
    }
    
    /**
     * Instantiates a new trusted proxy authentication trust store ssl socket factory.
     *
     * @param trustStoreFile the trust store file
     * @param trustStorePassword the trust store password
     * @param supportedProtocols the supported protocols
     * @param hostnameVerifier the hostname verifier
     * @param trustStoreType the keystore type
     */
    public TrustedProxyAuthenticationTrustStoreSSLSocketFactory(final File trustStoreFile, final String trustStorePassword,
            final List<String> supportedProtocols, final X509HostnameVerifier hostnameVerifier, final String trustStoreType) {
        this.trustStoreFile = trustStoreFile;
        this.trustStorePassword = trustStorePassword;
        this.supportedProtocols = supportedProtocols;
        this.hostnameVerifier = hostnameVerifier;
        this.trustStoreType = trustStoreType;
    }

    /**
     * Creates the {@link SSLConnectionSocketFactory}
     * instance based on truststore settings configured.
     *
     * @return the SSL connection socket factory, or null
     */
    public SSLConnectionSocketFactory createInstance() {
        KeyStore trustStore = null;
        FileInputStream instream = null;
        try {
            
            if (!trustStoreFile.exists() || !trustStoreFile.canRead()) {
                throw new FileNotFoundException("Truststore file cannot be located at " + trustStoreFile.getCanonicalPath());
            }
            trustStore = KeyStore.getInstance(this.trustStoreType);
            instream = new FileInputStream(this.trustStoreFile);
            trustStore.load(instream, this.trustStorePassword.toCharArray());
            
            LOGGER.debug("Loaded truststore file [{}] for proxy authentication", this.trustStoreFile);
            final SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore,
                    new TrustSelfSignedStrategy()).build();
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                    this.supportedProtocols.toArray(new String[] {}), null,
                    this.hostnameVerifier);
            return sslsf;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(instream);
        }
    }

}
