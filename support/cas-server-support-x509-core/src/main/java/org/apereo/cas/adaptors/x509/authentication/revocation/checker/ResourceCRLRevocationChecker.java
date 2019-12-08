package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.RevocationPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CRL-based revocation checker that uses one or more CRL resources to fetch
 * local or remote CRL data periodically.  CRL resources should be supplied for
 * the issuers of all certificates (and intervening certificates for certificate
 * chains) that are expected to be presented to {@link X509CredentialsAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @since 3.4.7
 */
@Slf4j
public class ResourceCRLRevocationChecker extends AbstractCRLRevocationChecker implements InitializingBean, DisposableBean {

    private static final int DEFAULT_REFRESH_INTERVAL = 3600;


    /**
     * Executor responsible for refreshing CRL data.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * CRL refresh interval in seconds.
     */
    private final int refreshInterval;

    private final CRLFetcher fetcher;

    /**
     * Map of CRL issuer to CRL.
     */
    private final Map<X500Principal, X509CRL> crlIssuerMap = Collections.synchronizedMap(new HashMap<>(0));

    /**
     * Resource CRLs.
     **/
    private final Collection<Resource> resources;

    public ResourceCRLRevocationChecker(final boolean checkAll, final RevocationPolicy<Void> unavailableCRLPolicy,
                                        final RevocationPolicy<X509CRL> expiredCRLPolicy, final int refreshInterval,
                                        final CRLFetcher fetcher, final Collection<Resource> resources) {
        super(checkAll, unavailableCRLPolicy, expiredCRLPolicy);
        this.refreshInterval = refreshInterval;
        this.fetcher = fetcher;
        this.resources = resources;
    }

    public ResourceCRLRevocationChecker(final Resource crl,
                                        final RevocationPolicy<Void> unavailableCRLPolicy,
                                        final RevocationPolicy<X509CRL> expiredCRLPolicy) {
        this(false, unavailableCRLPolicy, expiredCRLPolicy, DEFAULT_REFRESH_INTERVAL,
            new ResourceCRLFetcher(), CollectionUtils.wrap(crl));

    }

    public ResourceCRLRevocationChecker(final Resource[] crl,
                                        final RevocationPolicy<X509CRL> expiredCRLPolicy) {
        this(false, null, expiredCRLPolicy, DEFAULT_REFRESH_INTERVAL,
            new ResourceCRLFetcher(), CollectionUtils.wrapList(crl));

    }

    /**
     * Creates a new instance using the specified resource for CRL data.
     *
     * @param crl Resource containing CRL data.  MUST NOT be null.
     */
    public ResourceCRLRevocationChecker(final Resource crl) {
        this(CollectionUtils.wrap(crl));
    }

    /**
     * Creates a new instance using the specified resources for CRL data.
     *
     * @param crls Resources containing CRL data.  MUST NOT be null and MUST have
     *             at least one non-null element.
     */
    public ResourceCRLRevocationChecker(final Collection<Resource> crls) {
        this(new ResourceCRLFetcher(), crls, DEFAULT_REFRESH_INTERVAL);
    }

    public ResourceCRLRevocationChecker(final Resource... crls) {
        this(new ResourceCRLFetcher(), CollectionUtils.wrapList(crls), DEFAULT_REFRESH_INTERVAL);
    }

    /**
     * Instantiates a new Resource cRL revocation checker.
     *
     * @param fetcher         the fetcher
     * @param crls            the crls
     * @param refreshInterval the refresh interval
     * @since 4.1
     */
    public ResourceCRLRevocationChecker(final CRLFetcher fetcher, final Collection<Resource> crls, final int refreshInterval) {
        this(false, null, null, refreshInterval, fetcher, crls);
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    /**
     * Initializes the process that periodically fetches CRL data.
     */
    @SneakyThrows
    @SuppressWarnings("FutureReturnValueIgnored")
    public void init() {
        if (!validateConfiguration()) {
            return;
        }

        val results = this.fetcher.fetch(getResources());
        ResourceCRLRevocationChecker.this.addCrls(results);

        final Runnable scheduledFetcher = () -> {
            try {
                val fetchedResults = getFetcher().fetch(getResources());
                ResourceCRLRevocationChecker.this.addCrls(fetchedResults);
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        };

        this.scheduler.scheduleAtFixedRate(
            scheduledFetcher,
            this.refreshInterval,
            this.refreshInterval,
            TimeUnit.SECONDS);

    }

    private boolean validateConfiguration() {
        if (this.resources == null || this.resources.isEmpty()) {
            LOGGER.debug("[{}] is not configured with resources. Skipping configuration...",
                this.getClass().getSimpleName());
            return false;
        }
        if (this.fetcher == null) {
            LOGGER.debug("[{}] is not configured with a CRL fetcher. Skipping configuration...", getClass().getSimpleName());
            return false;
        }
        if (getExpiredCRLPolicy() == null) {
            LOGGER.debug("[{}] is not configured with a CRL expiration policy. Skipping configuration...", getClass().getSimpleName());
            return false;
        }
        if (getUnavailableCRLPolicy() == null) {
            LOGGER.debug("[{}] is not configured with a CRL unavailable policy. Skipping configuration...", getClass().getSimpleName());
            return false;
        }
        return true;
    }

    /**
     * Add fetched crls to the map.
     *
     * @param results the results
     */
    private void addCrls(final Collection<X509CRL> results) {
        results.forEach(entry -> addCRL(entry.getIssuerX500Principal(), entry));
    }

    /**
     * @return Returns the CRL fetcher component.
     */
    protected CRLFetcher getFetcher() {
        return this.fetcher;
    }

    protected Collection<Resource> getResources() {
        return this.resources;
    }

    @Override
    protected boolean addCRL(final Object issuer, final X509CRL crl) {
        LOGGER.debug("Adding CRL for issuer [{}]", issuer);
        this.crlIssuerMap.put((X500Principal) issuer, crl);
        return this.crlIssuerMap.containsKey(issuer);
    }

    @Override
    protected Collection<X509CRL> getCRLs(final X509Certificate cert) {
        val principal = cert.getIssuerX500Principal();

        if (this.crlIssuerMap.containsKey(principal)) {
            return CollectionUtils.wrap(this.crlIssuerMap.get(principal));
        }
        LOGGER.warn("Could not locate CRL for issuer principal [{}]", principal);
        return new ArrayList<>(0);
    }

    @Override
    public void destroy() {
        shutdown();
    }

    /**
     * Shutdown scheduler.
     */
    public void shutdown() {
        this.scheduler.shutdown();
    }
}
