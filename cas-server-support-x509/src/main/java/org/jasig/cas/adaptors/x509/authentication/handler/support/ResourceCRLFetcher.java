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

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509CRL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the fetching of CRL objects based on resources.
 * Supports http/ldap resources.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ResourceCRLFetcher implements CRLFetcher {
    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new instance using the specified resources for CRL data.
     */
    public ResourceCRLFetcher() {}

    @Override
    public final Set<X509CRL> fetch(final Set<? extends Object> crls) throws Exception {
        final Set<X509CRL> results = new HashSet<>();
        for (final Object r : crls) {
            logger.debug("Fetching CRL data from {}", r);
            final X509CRL crl = fetchInternal(r);
            if (crl != null) {
                results.add(crl);
            }
        }
        return results;
    }

    @Override
    public X509CRL fetch(final Object crl) throws Exception {
        final Set<X509CRL> results = fetch(Collections.singleton(crl));
        if (results.size() > 0) {
            return results.iterator().next();
        }
        logger.warn("Unable to fetch {}", crl);
        return null;
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param r the resource which can be {@link URL}, {@link URI}, {@link String}
     *          or {@link AbstractResource}
     * @return the x 509 cRL
     * @throws Exception the exception
     */
    protected X509CRL fetchInternal(@NotNull final Object r) throws Exception {
        Resource rs = null;
        if (r instanceof URI) {
            rs = new UrlResource(((URI) r).toURL());
        } else if (r instanceof URL) {
            rs = new UrlResource(((URL) r));
        } else if (r instanceof AbstractResource) {
            rs = (AbstractResource) r;
        } else if (r instanceof String) {
            rs = new UrlResource(new URL(r.toString()));
        }

        if (rs == null) {
            throw new IllegalArgumentException("Resource " + r + " could not be identified");
        }

        try (final InputStream ins = rs.getInputStream()) {
            return (X509CRL) CertUtils.getCertificateFactory().generateCRL(ins);
        }
    }
}
