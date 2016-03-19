package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
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
@Component("resourceCrlFetcher")
public class ResourceCRLFetcher implements CRLFetcher {
    /**
     * Logger instance.
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new instance using the specified resources for CRL data.
     */
    public ResourceCRLFetcher() {
    }

    @Override
    public Set<X509CRL> fetch(final Set<Resource> crls) throws IOException, CRLException, CertificateException {
        final Set<X509CRL> results = new HashSet<>();
        for (final Resource r : crls) {
            logger.debug("Fetching CRL data from {}", r);
            try (final InputStream ins = r.getInputStream()) {
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
    public X509CRL fetch(@NotNull final String crl) throws IOException, CRLException, CertificateException {
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
    public X509CRL fetch(@NotNull final Resource crl) throws IOException, CRLException, CertificateException {
        final Set<X509CRL> results = fetch(Collections.singleton(crl));
        if (!results.isEmpty()) {
            return results.iterator().next();
        }
        logger.warn("Unable to fetch {}", crl);
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
    public X509CRL fetch(@NotNull final URI crl) throws IOException, CRLException, CertificateException {
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
    public X509CRL fetch(@NotNull final URL crl) throws IOException, CRLException, CertificateException {
        return fetch(new UrlResource((URL) crl));
    }
}
