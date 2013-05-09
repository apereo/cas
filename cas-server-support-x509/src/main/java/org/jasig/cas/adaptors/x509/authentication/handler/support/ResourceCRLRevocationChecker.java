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

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.x500.X500Principal;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * CRL-based revocation checker that uses one or more CRL resources to fetch
 * local or remote CRL data periodically.  CRL resources should be supplied for
 * the issuers of all certificates (and intervening certificates for certificate
 * chains) that are expected to be presented to {@link X509CredentialsAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @since 3.4.7
 *
 */
public class ResourceCRLRevocationChecker extends AbstractCRLRevocationChecker
            implements InitializingBean {

    /** Default refresh interval is 1 hour. */
    public static final int DEFAULT_REFRESH_INTERVAL = 3600;

    /** Executor responsible for refreshing CRL data. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** CRL refresh interval in seconds. */
    private int refreshInterval = DEFAULT_REFRESH_INTERVAL;

    /** Handles fetching CRL data. */
    private final CRLFetcher fetcher;

    /** Map of CRL issuer to CRL. */
    private final Map<X500Principal, X509CRL> crlIssuerMap =
            Collections.synchronizedMap(new HashMap<X500Principal, X509CRL>());

    /**
     * Creates a new instance using the specified resource for CRL data.
     *
     * @param crl Resource containing CRL data.  MUST NOT be null.
     */
    public ResourceCRLRevocationChecker(final Resource crl) {
        this(new Resource[] {crl});
    }

    /**
     * Creates a new instance using the specified resources for CRL data.
     *
     * @param crls Resources containing CRL data.  MUST NOT be null and MUST have
     * at least one non-null element.
     */
    public ResourceCRLRevocationChecker(final Resource[] crls) {
        this.fetcher = new CRLFetcher(crls);
    }

    /**
     * Sets the interval at which CRL data should be reloaded from CRL resources.
     *
     * @param seconds Refresh interval in seconds; MUST be positive integer.
     */
    public void setRefreshInterval(final int seconds) {
        if (seconds > 0) {
            this.refreshInterval = seconds;
        } else {
            throw new IllegalArgumentException("Refresh interval must be positive integer.");
        }
    }

    /**
     * {@inheritDoc}
     * Initializes the process that periodically fetches CRL data. */
    @Override
    public void afterPropertiesSet() throws Exception {
        // Fetch CRL data synchronously and throw exception to abort if any fail
        this.fetcher.fetch(true);

        // Set up the scheduler to fetch periodically to implement refresh
        final Runnable scheduledFetcher = new Runnable() {
            @Override
            public void run() {
                getFetcher().fetch(false);
            }
        };
        this.scheduler.scheduleAtFixedRate(
                scheduledFetcher, this.refreshInterval, this.refreshInterval, TimeUnit.SECONDS);
    }

    /**
     * @return Returns the CRL fetcher component.
     */
    protected CRLFetcher getFetcher() {
        return this.fetcher;
    }

    /**
     * Adds the given CRL to the collection of CRLs held by this class.
     *
     * @param crl The crl to add
     */
    protected void addCrl(final X509CRL crl) {
        final X500Principal issuer = crl.getIssuerX500Principal();
        logger.debug("Adding CRL for issuer {}", issuer);
        this.crlIssuerMap.put(issuer, crl);
    }

    /**
     * {@inheritDoc}
     * @see AbstractCRLRevocationChecker#getCRL(X509Certificate)
     */
    @Override
    protected X509CRL getCRL(final X509Certificate cert) {
        return this.crlIssuerMap.get(cert.getIssuerX500Principal());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.scheduler.shutdown();
    }


    /**
     * Handles details of fetching CRL data from resources.
     */
    protected class CRLFetcher {
        /** Logger instance. */
        private final Logger logger = LoggerFactory.getLogger(getClass());

        /** Array of resources pointing to CRL data. */
        private final List<Resource> resources;

        /**
         * Creates a new instance using the specified resources for CRL data.
         *
         * @param crls Resources containing CRL data.  MUST NOT be null and MUST have
         * at least one non-null element.
         */
        public CRLFetcher(final Resource[] crls) {
            if (crls == null) {
                throw new IllegalArgumentException("CRL resources cannot be null.");
            }
            this.resources = new ArrayList<Resource>();
            for (Resource r : crls) {
                if (r != null) {
                    this.resources.add(r);
                }
            }
            if (this.resources.size() == 0) {
                throw new IllegalArgumentException("Must provide at least one non-null CRL resource.");
            }
        }

        /**
         * Fetches CRL data for all resources held by this instance.
         *
         * @param throwOnError Set to true to throw on first error fetching CRL
         * data, false otherwise.
         */
        public void fetch(final boolean throwOnError) {
            for (Resource r : this.resources) {
                logger.debug("Fetching CRL data from {}", r);
                try {
                    addCrl(CertUtils.fetchCRL(r));
                } catch (final Exception e) {
                    if (throwOnError) {
                        throw new RuntimeException("Error fetching CRL from " + r, e);
                    }
                }
            }
        }
    }
}
