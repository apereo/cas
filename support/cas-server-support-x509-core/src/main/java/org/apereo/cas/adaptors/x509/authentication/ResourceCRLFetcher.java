package org.apereo.cas.adaptors.x509.authentication;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.URI;
import java.net.URL;
import java.security.cert.X509CRL;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handles the fetching of CRL objects based on resources.
 * Supports http/ldap resources.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class ResourceCRLFetcher implements CRLFetcher {
    @Override
    public Collection<X509CRL> fetch(final Collection<Resource> crls) {
        return crls.stream()
            .map(Unchecked.function(crl -> {
                LOGGER.debug("Fetching CRL data from [{}]", crl);
                try (val ins = crl.getInputStream()) {
                    return (X509CRL) CertUtils.getCertificateFactory().generateCRL(ins);
                }
            }))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }


    @Override
    public X509CRL fetch(final String crl) throws Exception {
        return fetch(new URL(crl));
    }

    @Override
    public X509CRL fetch(final URI crl) throws Exception {
        return fetch(crl.toURL());
    }

    @Override
    public X509CRL fetch(final URL crl) throws Exception {
        return fetch(new UrlResource(crl));
    }

    @Override
    public X509CRL fetch(final Resource crl) throws Exception {
        val results = fetch(CollectionUtils.wrap(crl));
        if (!results.isEmpty()) {
            return results.iterator().next();
        }
        LOGGER.warn("Unable to fetch [{}]", crl);
        return null;
    }
}
