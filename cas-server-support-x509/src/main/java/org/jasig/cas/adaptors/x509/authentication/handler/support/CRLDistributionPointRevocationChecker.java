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
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.net.URI;
import java.net.URL;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.springframework.core.io.UrlResource;

import edu.vt.middleware.crypt.x509.ExtensionReader;
import edu.vt.middleware.crypt.x509.types.DistributionPoint;
import edu.vt.middleware.crypt.x509.types.DistributionPointList;
import edu.vt.middleware.crypt.x509.types.GeneralName;
import edu.vt.middleware.crypt.x509.types.GeneralNameList;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;


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


    /**
     * Creates a new instance that uses the given cache instance for CRL caching.
     *
     * @param crlCache Cache for CRL data.
     */
    public CRLDistributionPointRevocationChecker(final Cache crlCache) {
        if (crlCache == null) {
            throw new IllegalArgumentException("Cache cannot be null.");
        }
        this.crlCache = crlCache;
    }

    /**
     * {@inheritDoc}
     * @see AbstractCRLRevocationChecker#getCRL(X509Certificate)
     */
    @Override
    protected X509CRL getCRL(final X509Certificate cert) {
        final URL[] urls = getDistributionPoints(cert);
        logger.debug(String.format(
                "Distribution points for %s: %s.",
                CertUtils.toString(cert), Arrays.asList(urls)));

        Element item;
        for (URL url : urls) {
            item = this.crlCache.get(url);
            if (item != null) {
                logger.debug("Found CRL in cache for {}", CertUtils.toString(cert));
                return (X509CRL) item.getObjectValue();
            }
        }

        // Try all distribution points and stop at first fetch that succeeds
        X509CRL crl = null;
        for (int i = 0; i < urls.length && crl == null; i++) {
            logger.info("Attempting to fetch CRL at {}", urls[i]);
            try {
                crl = CertUtils.fetchCRL(new UrlResource(urls[i]));
                logger.info("Success. Caching fetched CRL.");
                this.crlCache.put(new Element(urls[i], crl));
            } catch (final Exception e) {
                logger.error("Error fetching CRL at {}", urls[i], e);
            }
        }

        return crl;
    }

    private URL[] getDistributionPoints(final X509Certificate cert) {
        final DistributionPointList points;
        try {
            points = new ExtensionReader(cert).readCRLDistributionPoints();
        } catch (final Exception e) {
            logger.error(
                    "Error reading CRLDistributionPoints extension field on " + CertUtils.toString(cert), e);
            return new URL[0];
        }

        final List<URL> urls = new ArrayList<URL>();
        for (DistributionPoint point : points.getItems()) {
            final Object location = point.getDistributionPoint();
            if (location instanceof String) {
                addURL(urls, (String) location);
            } else if (location instanceof GeneralNameList) {
                for (GeneralName gn : ((GeneralNameList) location).getItems()) {
                    addURL(urls, gn.getName());
                }
            } else {
                logger.warn("{} not supported. String or GeneralNameList expected.", location);
            }
        }

        return urls.toArray(new URL[urls.size()]);
    }

    private void addURL(final List<URL> list, final String uriString) {
        try {
            // Build URI by components to facilitate proper encoding of querystring
            // e.g. http://example.com:8085/ca?action=crl&issuer=CN=CAS Test User CA
            final URL url = new URL(uriString);
            final URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
            list.add(uri.toURL());
        } catch (final Exception e) {
            logger.warn("{} is not a valid distribution point URI.", uriString);
        }
    }
}
