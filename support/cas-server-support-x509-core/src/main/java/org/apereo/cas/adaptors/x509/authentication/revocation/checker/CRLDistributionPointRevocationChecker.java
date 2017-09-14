package org.apereo.cas.adaptors.x509.authentication.revocation.checker;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.RevocationPolicy;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.CollectionUtils;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.cryptacular.x509.ExtensionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CRLDistributionPointRevocationChecker extends AbstractCRLRevocationChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CRLDistributionPointRevocationChecker.class);
    
    private final Cache crlCache;
    private final CRLFetcher fetcher;
    private final boolean throwOnFetchFailure;

    /**
     * Creates a new instance that uses the given cache instance for CRL caching.
     *
     * @param crlCache Cache for CRL data.
     */
    public CRLDistributionPointRevocationChecker(final Cache crlCache) {
        this(crlCache, new ResourceCRLFetcher(), false);
    }

    /**
     * Creates a new instance that uses the given cache instance for CRL caching.
     *
     * @param crlCache            Cache for CRL data.
     * @param throwOnFetchFailure the throw on fetch failure
     */
    public CRLDistributionPointRevocationChecker(final Cache crlCache, final boolean throwOnFetchFailure) {
        this(crlCache, new ResourceCRLFetcher(), throwOnFetchFailure);
    }

    /**
     * Instantiates a new CRL distribution point revocation checker.
     *
     * @param crlCache            the crl cache
     * @param fetcher             the fetcher
     * @param throwOnFetchFailure the throw on fetch failure
     */
    public CRLDistributionPointRevocationChecker(
            final Cache crlCache, final CRLFetcher fetcher, final boolean throwOnFetchFailure) {
        this(false, null, null, crlCache, fetcher, throwOnFetchFailure);
    }

    public CRLDistributionPointRevocationChecker(final Cache crlCache,
                                                 final RevocationPolicy<Void> unavailableCRLPolicy) {
        this(crlCache, null, unavailableCRLPolicy);
    }

    public CRLDistributionPointRevocationChecker(final Cache crlCache,
                                                 final RevocationPolicy<X509CRL> expiredCRLPolicy,
                                                 final RevocationPolicy<Void> unavailableCRLPolicy) {
        this(crlCache, expiredCRLPolicy, unavailableCRLPolicy, false);
    }

    public CRLDistributionPointRevocationChecker(final Cache crlCache,
                                                 final RevocationPolicy<X509CRL> expiredCRLPolicy,
                                                 final RevocationPolicy<Void> unavailableCRLPolicy,
                                                 final boolean throwOnFetchFailure) {
        this(false, unavailableCRLPolicy, expiredCRLPolicy, crlCache, new ResourceCRLFetcher(), throwOnFetchFailure);
    }
    
    public CRLDistributionPointRevocationChecker(final boolean checkAll, final RevocationPolicy<Void> unavailableCRLPolicy,
                                                 final RevocationPolicy<X509CRL> expiredCRLPolicy, final Cache crlCache,
                                                 final CRLFetcher fetcher, final boolean throwOnFetchFailure) {
        super(checkAll, unavailableCRLPolicy, expiredCRLPolicy);
        this.crlCache = crlCache;
        this.fetcher = fetcher;
        this.throwOnFetchFailure = throwOnFetchFailure;
    }

    @Override
    protected List<X509CRL> getCRLs(final X509Certificate cert) {

        if (this.crlCache == null) {
            throw new IllegalArgumentException("CRL cache is not defined");
        }
        if (this.fetcher == null) {
            throw new IllegalArgumentException("CRL fetcher is not defined");
        }
        if (getExpiredCRLPolicy() == null) {
            throw new IllegalArgumentException("Expiration CRL policy is not defined");
        }
        if (getUnavailableCRLPolicy() == null) {
            throw new IllegalArgumentException("Unavailable CRL policy is not defined");
        }

        final URI[] urls = getDistributionPoints(cert);
        LOGGER.debug("Distribution points for [{}]: [{}].", CertUtils.toString(cert), CollectionUtils.wrap(urls));
        final List<X509CRL> listOfLocations = new ArrayList<>(urls.length);
        boolean stopFetching = false;

        try {
            for (int index = 0; !stopFetching && index < urls.length; index++) {
                final URI url = urls[index];
                final Element item = this.crlCache.get(url);

                if (item != null) {
                    LOGGER.debug("Found CRL in cache for [{}]", CertUtils.toString(cert));
                    final byte[] encodedCrl = (byte[]) item.getObjectValue();
                    final X509CRL crlFetched = this.fetcher.fetch(new ByteArrayResource(encodedCrl));

                    if (crlFetched != null) {
                        listOfLocations.add(crlFetched);
                    } else {
                        LOGGER.warn("Could fetch X509 CRL for [{}]. Returned value is null", url);
                    }
                } else {
                    LOGGER.debug("CRL for [{}] is not cached. Fetching and caching...", CertUtils.toString(cert));
                    try {
                        final X509CRL crl = this.fetcher.fetch(url);
                        if (crl != null) {
                            LOGGER.info("Success. Caching fetched CRL at [{}].", url);
                            addCRL(url, crl);
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
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        LOGGER.debug("Found [{}] CRLs", listOfLocations.size());
        return listOfLocations;
    }

    @Override
    protected boolean addCRL(final Object id, final X509CRL crl) {
        try {
            if (crl == null) {
                LOGGER.debug("No CRL was passed. Removing [{}] from cache...", id);
                return this.crlCache.remove(id);
            }

            this.crlCache.put(new Element(id, crl.getEncoded()));
            return this.crlCache.get(id) != null;

        } catch (final Exception e) {
            LOGGER.warn("Failed to add the crl entry [{}] to the cache", crl);
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * Gets the distribution points.
     *
     * @param cert the cert
     * @return the url distribution points
     */
    private static URI[] getDistributionPoints(final X509Certificate cert) {
        final List<DistributionPoint> points;
        try {
            points = new ExtensionReader(cert).readCRLDistributionPoints();
        } catch (final Exception e) {
            LOGGER.error("Error reading CRLDistributionPoints extension field on [{}]", CertUtils.toString(cert), e);
            return new URI[0];
        }

        final List<URI> urls = new ArrayList<>();

        if (points != null) {
            points.stream().map(DistributionPoint::getDistributionPoint).filter(Objects::nonNull).forEach(pointName -> {
                final ASN1Sequence nameSequence = ASN1Sequence.getInstance(pointName.getName());
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

        return urls.toArray(new URI[urls.size()]);
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
            URI uri;
            try {
                final URL url = new URL(URLDecoder.decode(uriString, StandardCharsets.UTF_8.name()));
                uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
            } catch (final MalformedURLException e) {
                uri = new URI(uriString);
            }
            list.add(uri);
        } catch (final Exception e) {
            LOGGER.warn("[{}] is not a valid distribution point URI.", uriString);
        }
    }


}
