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
package org.jasig.cas.adaptors.x509.authentication.handler.support;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.springframework.core.io.ByteArrayResource;

import edu.vt.middleware.crypt.x509.ExtensionReader;
import edu.vt.middleware.crypt.x509.types.DistributionPoint;
import edu.vt.middleware.crypt.x509.types.DistributionPointList;
import edu.vt.middleware.crypt.x509.types.GeneralName;
import edu.vt.middleware.crypt.x509.types.GeneralNameList;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import javax.validation.constraints.NotNull;

/**
 * Performs CRL-based revocation checking by consulting resources defined in
 * the CRLDistributionPoints extension field on the certificate.  Although RFC
 * 2459 allows the distribution point name to have arbitrary meaning, this class
 * expects the name to define an absolute URL, which is the most common
 * implementation.  This implementation caches CRL resources fetched from remote
 * URLs to improve performance by avoiding CRL fetching on every revocation
 * check.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class CRLDistributionPointRevocationChecker extends AbstractCRLRevocationChecker {

    /** CRL cache. */
    private final Cache crlCache;

    /** The CRL fetcher instance. **/
    private final CRLFetcher fetcher;

    private boolean throwOnFetchFailure;

    /**
     * Creates a new instance that uses the given cache instance for CRL caching.
     *
     * @param crlCache Cache for CRL data.
     */
    public CRLDistributionPointRevocationChecker(@NotNull final Cache crlCache) {
        this(crlCache, new ResourceCRLFetcher());
    }

    /**
     * Instantiates a new CRL distribution point revocation checker.
     *
     * @param crlCache the crl cache
     * @param fetcher the fetcher
     */
    public CRLDistributionPointRevocationChecker(@NotNull final Cache crlCache,
                                                 @NotNull final CRLFetcher fetcher) {
        this.crlCache = crlCache;
        this.fetcher = fetcher;
    }


    /**
     * Throws exceptions if fetching crl fails. Defaults to false.
     *
     * @param throwOnFetchFailure the throw on fetch failure
     */
    public void setThrowOnFetchFailure(final boolean throwOnFetchFailure) {
        this.throwOnFetchFailure = throwOnFetchFailure;
    }

    /**
     * {@inheritDoc}
     * @see AbstractCRLRevocationChecker#getCRL(X509Certificate)
     */
    @Override
    protected X509CRL getCRL(final X509Certificate cert) {
        final URI[] urls = getDistributionPoints(cert);
        logger.debug("Distribution points for {}: {}.", CertUtils.toString(cert), Arrays.asList(urls));

        try {
            for (final URI url : urls) {
                final Element item = this.crlCache.get(url);
                if (item != null) {
                    logger.debug("Found CRL in cache for {}", CertUtils.toString(cert));
                    final byte[] encodedCrl = (byte[]) item.getObjectValue();
                    return this.fetcher.fetch(new ByteArrayResource(encodedCrl));
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        // Try all distribution points and stop at first fetch that succeeds
        X509CRL crl = null;
        for (int i = 0; i < urls.length && crl == null; i++) {
            final URI url = urls[i];
            logger.info("Attempting to fetch CRL at {}", url);
            try {
                crl = this.fetcher.fetch(url);
                logger.info("Success. Caching fetched CRL at {}.", url);
                addCRL(url, crl);
            } catch (final Exception e) {
                logger.error("Error fetching CRL at {}", url, e);
                if (this.throwOnFetchFailure) {
                    throw new RuntimeException(e);
                }
            }
        }

        return crl;
    }

    @Override
    protected boolean addCRL(final Object id, final X509CRL crl) {
        try {
            this.crlCache.put(new Element(id, crl.getEncoded()));
            return this.crlCache.get(id) != null;
        } catch (final Exception e) {
            logger.warn("Failed to add the crl entry [{}] to the cache", crl);
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the distribution points.
     *
     * @param cert the cert
     * @return the url distribution points
     */
    private URI[] getDistributionPoints(final X509Certificate cert) {
        final DistributionPointList points;
        try {
            points = new ExtensionReader(cert).readCRLDistributionPoints();
        } catch (final Exception e) {
            logger.error("Error reading CRLDistributionPoints extension field on {}", CertUtils.toString(cert), e);
            return new URI[0];
        }

        final List<URI> urls = new ArrayList<>();
        for (final DistributionPoint point : points.getItems()) {
            final Object location = point.getDistributionPoint();
            if (location instanceof String) {
                addURL(urls, (String) location);
            } else if (location instanceof GeneralNameList) {
                for (final GeneralName gn : ((GeneralNameList) location).getItems()) {
                    addURL(urls, gn.getName());
                }
            } else {
                logger.warn("{} not supported. String or GeneralNameList expected.", location);
            }
        }

        return urls.toArray(new URI[urls.size()]);
    }

    /**
     * Adds the url to the list.
     * Build URI by components to facilitate proper encoding of querystring.
     * e.g. http://example.com:8085/ca?action=crl&issuer=CN=CAS Test User CA
     *
     * <p>If <code>uriString</code> is encoded, it will be decoded with <code>UTF-8</code>
     * first before it's added to the list.</p>
     * @param list the list
     * @param uriString the uri string
     */
    private void addURL(final List<URI> list, final String uriString) {
        try {
            URI uri = null;
            try {
                final URL url = new URL(URLDecoder.decode(uriString, "UTF-8"));
                uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
            } catch (final MalformedURLException e) {
                uri = new URI(uriString);
            }
            list.add(uri);
        } catch (final Exception e) {
            logger.warn("{} is not a valid distribution point URI.", uriString);
        }
    }

}
