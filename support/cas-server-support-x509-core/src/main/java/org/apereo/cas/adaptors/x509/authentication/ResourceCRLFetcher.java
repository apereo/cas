package org.apereo.cas.adaptors.x509.authentication;

import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the fetching of CRL objects based on resources.
 * Supports http/ldap resources.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ResourceCRLFetcher implements CRLFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCRLFetcher.class);
    
    @Override
    public Collection<X509CRL> fetch(final Collection<Resource> crls) throws IOException, CRLException {
        final Set<X509CRL> results = new HashSet<>();
        for (final Resource r : crls) {
            LOGGER.debug("Fetching CRL data from [{}]", r);
            try(InputStream ins = r.getInputStream()) {
                final X509CRL crl = (X509CRL) CertUtils.getCertificateFactory().generateCRL(ins);
                if (crl != null) {
                    results.add(crl);
                }
            }
        }
        return results;
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final String crl) throws IOException, CRLException, CertificateException {
        return fetch(new URL(crl));
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final Resource crl) throws IOException, CRLException, CertificateException {
        final Collection<X509CRL> results = fetch(CollectionUtils.wrap(crl));
        if (!results.isEmpty()) {
            return results.iterator().next();
        }
        LOGGER.warn("Unable to fetch [{}]", crl);
        return null;
    }


    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final URI crl) throws IOException, CRLException, CertificateException {
        return fetch(crl.toURL());
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    public X509CRL fetch(final URL crl) throws IOException, CRLException, CertificateException {
        return fetch(new UrlResource(crl));
    }
}
