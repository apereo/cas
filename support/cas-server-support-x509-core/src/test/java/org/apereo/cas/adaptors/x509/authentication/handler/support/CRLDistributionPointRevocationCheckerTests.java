package org.apereo.cas.adaptors.x509.authentication.handler.support;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.adaptors.x509.util.MockWebServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Unit test for {@link CRLDistributionPointRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.76
 */
@RunWith(Parameterized.class)
public class CRLDistributionPointRevocationCheckerTests extends AbstractCRLRevocationCheckerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(CRLDistributionPointRevocationCheckerTests.class);

    /**
     * Instance under test.
     */
    private final CRLDistributionPointRevocationChecker checker;

    /**
     * Answers requests for CRLs made to localhost:8085.
     */
    private final MockWebServer webServer;


    /**
     * Creates a new test instance with given parameters.
     *
     * @param checker   Revocation checker instance.
     * @param certFiles File names of certificates to check.
     * @param crlFile   File name of CRL file to serve out.
     * @param expected  Expected result of check; null to indicate expected success.
     */
    public CRLDistributionPointRevocationCheckerTests(
            final CRLDistributionPointRevocationChecker checker,
            final String[] certFiles,
            final String crlFile,
            final GeneralSecurityException expected) throws Exception {

        super(certFiles, expected);

        final File file = new File(System.getProperty("java.io.tmpdir"), "ca.crl");
        if (file.exists()) {
            file.delete();
        }
        final OutputStream out = new FileOutputStream(file);
        IOUtils.copy(new ClassPathResource(crlFile).getInputStream(), out);

        this.checker = checker;

        this.webServer = new MockWebServer(8085, new FileSystemResource(file), "text/plain");
        LOGGER.debug("Web server listening on port 8085 serving file [{}]", crlFile);
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {
        CacheManager.getInstance().removeAllCaches();
        final Collection<Object[]> params = new ArrayList<>();
        Cache cache;
        final ThresholdExpiredCRLRevocationPolicy defaultPolicy = new ThresholdExpiredCRLRevocationPolicy(0);
        final ThresholdExpiredCRLRevocationPolicy zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy(0);

        // Test case #0
        // Valid certificate on valid CRL data with encoded url
        cache = new Cache("crlCache-0", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null),
            new String[]{"uservalid-encoded-crl.crt"},
            "test ca.crl",
            null,
        });

        // Test case #1
        // Valid certificate on valid CRL data
        cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[] {
                new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null, true),
            new String[]{"user-valid-distcrl.crt"},
            "userCA-valid.crl",
            null,
    });


        // Test case #2
        // Revoked certificate on valid CRL data
        cache = new Cache("crlCache-2", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null),
            new String[]{"user-revoked-distcrl.crt"},
            "userCA-valid.crl",
            new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), new BigInteger("1")),
        });

        // Test case #3
        // Valid certificate on expired CRL data
        cache = new Cache("crlCache-3", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
                new CRLDistributionPointRevocationChecker(cache, zeroThresholdPolicy, null),
            new String[]{"user-valid-distcrl.crt"},
            "userCA-expired.crl",
            new ExpiredCRLException("test", ZonedDateTime.now(ZoneOffset.UTC)),
    });

        // Test case #4
        // Valid certificate on expired CRL data with custom expiration
        // policy to always allow expired CRL data
        cache = new Cache("crlCache-4", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
                new CRLDistributionPointRevocationChecker(cache, crl -> {}, null),
            new String[]{"user-valid-distcrl.crt"},
            "userCA-expired.crl",
            null,
    });

        // Test case #5
        // Valid certificate with no CRL distribution points defined but with
        // "AllowRevocationPolicy" set to allow unavailable CRL data
        cache = new Cache("crlCache-5", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        final CRLDistributionPointRevocationChecker checker5 =
                new CRLDistributionPointRevocationChecker(cache, defaultPolicy, new AllowRevocationPolicy());
        params.add(new Object[]{checker5,
            new String[]{"user-valid.crt"},
            "userCA-expired.crl",
            null,
    });

        // Test case #6
        // EJBCA test case
        // Revoked certificate with CRL distribution point URI that is technically
        // not a valid URI since the issuer DN in the query string is not encoded per
        // the escaping of reserved characters in RFC 2396.
        // Make sure we can convert given URI to valid URI and confirm it's revoked
        cache = new Cache("crlCache-6", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
                new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null),
            new String[]{"user-revoked-distcrl2.crt"},
            "userCA-valid.crl",
            new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), new BigInteger("1")),
    });

        return params;
    }

    /**
     * Called once before every test.
     *
     * @throws Exception On setup errors.
     */
    @Before
    public void setUp() throws Exception {
        this.webServer.start();
        Thread.sleep(500);
    }

    /**
     * Called once before every test.
     *
     */
    @After
    public void tearDown() {
        LOGGER.debug("Stopping web server...");
        this.webServer.stop();
        LOGGER.debug("Web server stopped [{}]", !this.webServer.isRunning());
    }

    @AfterClass
    public static void destroy() {
        final File file = new File("ca.crl");
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    protected RevocationChecker getChecker() {
        return this.checker;
    }
}
