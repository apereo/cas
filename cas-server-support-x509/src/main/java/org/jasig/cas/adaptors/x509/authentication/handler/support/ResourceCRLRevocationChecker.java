package org.jasig.cas.adaptors.x509.authentication.handler.support;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.x500.X500Principal;
import javax.validation.constraints.Min;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
 *
 */
@Component("resourceCrlRevocationChecker")
public class ResourceCRLRevocationChecker extends AbstractCRLRevocationChecker  {

    /** Default refresh interval is 1 hour. */
    public static final int DEFAULT_REFRESH_INTERVAL = 3600;

    /** Executor responsible for refreshing CRL data. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** CRL refresh interval in seconds. */
    private int refreshInterval = DEFAULT_REFRESH_INTERVAL;

    @Autowired
    private ApplicationContext applicationContext;

    /** Handles fetching CRL data. */
    @Autowired(required = false)
    @Qualifier("x509CrlFetcher")
    private CRLFetcher fetcher;

    /** Map of CRL issuer to CRL. */
    private final Map<X500Principal, X509CRL> crlIssuerMap =
            Collections.synchronizedMap(new HashMap<X500Principal, X509CRL>());

    /** Resource CRLs. **/
    private Set<Resource> resources;

    /** Used for serialization and auto wiring. */
    private ResourceCRLRevocationChecker() {}

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
        this(new ResourceCRLFetcher(), crls);
    }

    /**
     * Instantiates a new Resource cRL revocation checker.
     *
     * @param fetcher the fetcher
     * @param crls the crls
     * @since 4.1
     */
    public ResourceCRLRevocationChecker(final CRLFetcher fetcher,
                                        final Resource[] crls) {
        this.fetcher = fetcher;
        this.resources = ImmutableSet.copyOf(crls);
    }

    /**
     * Sets the interval at which CRL data should be reloaded from CRL resources.
     *
     * @param seconds Refresh interval in seconds; MUST be positive integer.
     */
    @Autowired
    public void setRefreshInterval(@Min(1) @Value("${cas.x509.authn.crl.refresh.interval:" + DEFAULT_REFRESH_INTERVAL + '}')
                                   final int seconds) {
        this.refreshInterval = seconds;
    }


    private void initializeResourcesFromContext() {
        try {
            this.resources = applicationContext.getBean("x509CrlResources", Set.class);
            logger.debug("Located {} CRL resources from configuration", resources.size());
        } catch (final Exception e) {
            logger.debug("[x509CrlResources] is not defined in the application context");
        }
    }

    /**
     * Initializes the process that periodically fetches CRL data.
     */
    @PostConstruct
    @Override
    public void init()  {
        super.init();

        initializeResourcesFromContext();
        if (!validateConfiguration()) {
            return;
        }

        try {
            // Fetch CRL data synchronously and throw exception to abort if any fail
            final Set<X509CRL> results = this.fetcher.fetch(getResources());
            ResourceCRLRevocationChecker.this.addCrls(results);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        // Set up the scheduler to fetch periodically to implement refresh
        final Runnable scheduledFetcher = new Runnable() {
            private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

            @Override
            public void run() {
                try {
                    final Set<Resource> resources = ResourceCRLRevocationChecker.this.getResources();
                    final Set<X509CRL> results = getFetcher().fetch(resources);
                    ResourceCRLRevocationChecker.this.addCrls(results);
                } catch (final Exception e) {
                    logger.debug(e.getMessage(), e);
                }
            }
        };
        this.scheduler.scheduleAtFixedRate(
                scheduledFetcher, this.refreshInterval, this.refreshInterval, TimeUnit.SECONDS);

    }

    private boolean validateConfiguration() {
        if (resources == null || resources.isEmpty()) {
            logger.debug("{} is not configured with resources. Skipping configuration...",
                    this.getClass().getSimpleName());
            return false;
        }
        if (this.fetcher == null) {
            logger.debug("{} is not configured with a CRL fetcher. Skipping configuration...",
                    this.getClass().getSimpleName());
            return false;
        }
        if (getExpiredCRLPolicy() == null) {
            logger.debug("{} is not configured with a CRL expiration policy. Skipping configuration...",
                    this.getClass().getSimpleName());
            return false;
        }
        if (getUnavailableCRLPolicy()== null) {
            logger.debug("{} is not configured with a CRL unavailable policy. Skipping configuration...",
                    this.getClass().getSimpleName());
            return false;
        }
        return true;
    }

    /**
     * Add fetched crls to the map.
     *
     * @param results the results
     */
    private void addCrls(final Set<X509CRL> results) {
        final Iterator<X509CRL> it = results.iterator();
        while (it.hasNext()) {
            final X509CRL entry = it.next();
            addCRL(entry.getIssuerX500Principal(), entry);
        }
    }

    /**
     * @return Returns the CRL fetcher component.
     */
    protected CRLFetcher getFetcher() {
        return this.fetcher;
    }

    protected Set<Resource> getResources() {
        return this.resources;
    }

    @Override
    protected boolean addCRL(final Object issuer, final X509CRL crl) {
        logger.debug("Adding CRL for issuer {}", issuer);
        this.crlIssuerMap.put((X500Principal) issuer, crl);
        return this.crlIssuerMap.containsKey(issuer);
    }

    @Override
    protected Collection<X509CRL> getCRLs(final X509Certificate cert) {
        return Collections.singleton(this.crlIssuerMap.get(cert.getIssuerX500Principal()));
    }

    /**
     * Shutdown scheduler.
     */
    @PreDestroy
    public void shutdown() {
        this.scheduler.shutdown();
    }

    @Autowired(required=false)
    @Override
    public void setUnavailableCRLPolicy(@Qualifier("x509ResourceUnavailableRevocationPolicy") final RevocationPolicy policy) {
        super.setUnavailableCRLPolicy(policy);
    }

    @Autowired(required=false)
    @Override
    public void setExpiredCRLPolicy(@Qualifier("x509ResourceExpiredRevocationPolicy") final RevocationPolicy policy) {
        super.setExpiredCRLPolicy(policy);
    }

}
