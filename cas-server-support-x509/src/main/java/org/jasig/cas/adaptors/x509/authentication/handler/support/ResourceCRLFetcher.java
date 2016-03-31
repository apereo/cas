package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

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
@Component("resourceCrlFetcher")
public class ResourceCRLFetcher implements CRLFetcher {
    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

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
