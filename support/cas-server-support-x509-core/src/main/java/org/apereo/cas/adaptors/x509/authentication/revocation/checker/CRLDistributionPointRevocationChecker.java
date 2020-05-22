package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.RevocationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.cryptacular.x509.ExtensionReader;
import org.ehcache.StateTransitionException;
import org.ehcache.Status;
import org.ehcache.UserManagedCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.ByteArrayResource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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
 */
@Slf4j
public class CRLDistributionPointRevocationChecker extends AbstractCRLRevocationChecker implements DisposableBean, AutoCloseable {

    private final UserManagedCache<URI, byte[]> crlCache;

    private final CRLFetcher fetcher;

    private final boolean throwOnFetchFailure;

    public CRLDistributionPointRevocationChecker(
        final UserManagedCache<URI, byte[]> crlCache, final CRLFetcher fetcher, final boolean throwOnFetchFailure) {
        this(false, null, null, crlCache, fetcher, throwOnFetchFailure);
    }

    public CRLDistributionPointRevocationChecker(final UserManagedCache<URI, byte[]> crlCache,
                                                 final RevocationPolicy<X509CRL> expiredCRLPolicy,
                                                 final RevocationPolicy<Void> unavailableCRLPolicy) {
        this(crlCache, expiredCRLPolicy, unavailableCRLPolicy, false);
    }

    public CRLDistributionPointRevocationChecker(final UserManagedCache<URI, byte[]> crlCache,
                                                 final RevocationPolicy<X509CRL> expiredCRLPolicy,
                                                 final RevocationPolicy<Void> unavailableCRLPolicy,
                                                 final boolean throwOnFetchFailure) {
        this(false, unavailableCRLPolicy, expiredCRLPolicy, crlCache, new ResourceCRLFetcher(), throwOnFetchFailure);
    }

    public CRLDistributionPointRevocationChecker(final boolean checkAll, final RevocationPolicy<Void> unavailableCRLPolicy,
                                                 final RevocationPolicy<X509CRL> expiredCRLPolicy,
                                                 final UserManagedCache<URI, byte[]> crlCache,
                                                 final CRLFetcher fetcher, final boolean throwOnFetchFailure) {
        super(checkAll, unavailableCRLPolicy, expiredCRLPolicy);
        this.crlCache = crlCache;
        if (Status.UNINITIALIZED.equals(this.crlCache.getStatus())) {
            this.crlCache.init();
        }
        this.fetcher = fetcher;
        this.throwOnFetchFailure = throwOnFetchFailure;
    }
    
    @Override
    public void destroy() {
        try {
            this.crlCache.close();
        } catch (final StateTransitionException e) {
            LOGGER.warn("Error closing CRL cache {}", e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        destroy();
    }

    /**
     * Gets the distribution points.
     *
     * @param cert the cert
     * @return the url distribution points
     */
    private static URI[] getDistributionPoints(final X509Certificate cert) {
        try {
            val points = new ExtensionReader(cert).readCRLDistributionPoints();
            val urls = new ArrayList<URI>(points == null ? 0 : points.size());
            if (points != null) {
                points.stream().map(DistributionPoint::getDistributionPoint).filter(Objects::nonNull).forEach(pointName -> {
                    val nameSequence = ASN1Sequence.getInstance(pointName.getName());
                    IntStream.range(0, nameSequence.size()).mapToObj(i -> GeneralName.getInstance(nameSequence.getObjectAt(i))).forEach(name -> {
                        LOGGER.debug("Found CRL distribution point [{}].", name);
                        try {
                            addURL(urls, DERIA5String.getInstance(name.getName()).getString());
                        } catch (final Exception e) {
                            LOGGER.warn("[{}] not supported. String or GeneralNameList expected.", pointName);
                        }
                    });
                });
            }
            return urls.toArray(URI[]::new);
        } catch (final Exception e) {
            LOGGER.error("Error reading CRLDistributionPoints extension field on [{}]", CertUtils.toString(cert), e);
            return new URI[0];
        }
    }

    /**
     * Adds the url to the list.
     * Build URI by components to facilitate proper encoding of querystring.
     * e.g. http://example.com:8085/ca?action=crl&issuer=CN=CAS Test User CA
     * <p>
     * <p>If {@code uriString} is encoded, it will be decoded with {@code UTF-8}
     * first before it's added to the list.</p>
     *
     * @param list      the list
     * @param uriString the uri string
     */
    private static void addURL(final List<URI> list, final String uriString) {
        try {
            try {
                val url = new URL(URLDecoder.decode(uriString, StandardCharsets.UTF_8.name()));
                list.add(new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null));
            } catch (final MalformedURLException e) {
                list.add(new URI(uriString));
            }
        } catch (final Exception e) {
            LOGGER.warn("[{}] is not a valid distribution point URI.", uriString);
        }
    }

    private boolean addCRLbyURI(final URI uri, final X509CRL crl) {
        return addCRL(uri, crl);
    }

    @Override
    @SneakyThrows
    protected List<X509CRL> getCRLs(final X509Certificate cert) {
        val urls = getDistributionPoints(cert);
        LOGGER.debug("Distribution points for [{}]: [{}].", CertUtils.toString(cert), CollectionUtils.wrap(urls));
        val listOfLocations = new ArrayList<X509CRL>(urls.length);
        var stopFetching = false;

        for (var index = 0; !stopFetching && index < urls.length; index++) {
            val url = urls[index];
            val item = this.crlCache.get(url);

            if (item != null) {
                LOGGER.debug("Found CRL in cache for [{}]", CertUtils.toString(cert));
                val crlFetched = this.fetcher.fetch(new ByteArrayResource(item));

                if (crlFetched != null) {
                    listOfLocations.add(crlFetched);
                } else {
                    LOGGER.warn("Could fetch X509 CRL for [{}]. Returned value is null", url);
                }
            } else {
                LOGGER.debug("CRL for [{}] is not cached. Fetching and caching...", CertUtils.toString(cert));
                try {
                    val crl = this.fetcher.fetch(url);
                    if (crl != null) {
                        LOGGER.info("Success. Caching fetched CRL at [{}].", url);
                        addCRLbyURI(url, crl);
                        listOfLocations.add(crl);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Error fetching CRL at [{}]", url, e);
                    if (this.throwOnFetchFailure) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }

            if (!this.checkAll && !listOfLocations.isEmpty()) {
                LOGGER.debug("CRL fetching is configured to not check all locations.");
                stopFetching = true;
            }

        }

        LOGGER.debug("Found [{}] CRLs", listOfLocations.size());
        return listOfLocations;
    }

    @Override
    @SneakyThrows
    protected boolean addCRL(final Object id, final X509CRL crl) {
        var uri = (URI) id;
        if (crl == null) {
            LOGGER.debug("No CRL was passed. Removing [{}] from cache...", id);
            this.crlCache.remove(uri);
            return false;
        }

        this.crlCache.put(uri, crl.getEncoded());
        return this.crlCache.containsKey(uri);
    }
}
