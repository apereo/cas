package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.util.MockWebServer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;


/**
 * Unit test for {@link CRLDistributionPointRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.76
 */
@Slf4j
@Tag("X509")
public class CRLDistributionPointRevocationCheckerTests extends BaseCRLRevocationCheckerTests {

    /**
     * Answers requests for CRLs made to localhost:8085.
     */
    private MockWebServer webServer;

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void checkCertificate(
        final CRLDistributionPointRevocationChecker checker,
        final String[] certFiles,
        final String crlFile,
        final GeneralSecurityException expected) throws IOException, InterruptedException {

        val file = new File(System.getProperty("java.io.tmpdir"), "ca.crl");
        val out = new FileOutputStream(file);
        IOUtils.copy(new ClassPathResource(crlFile).getInputStream(), out);

        this.webServer = new MockWebServer(8085, new FileSystemResource(file), "text/plain");

        this.webServer.start();
        LOGGER.debug("Web server listening on port 8085 serving file [{}]", crlFile);
        Thread.sleep(500);

        BaseCRLRevocationCheckerTests.checkCertificate(checker, certFiles, expected);
    }

    private static UserManagedCache<URI, byte[]> getCache(final int entries) {
        return UserManagedCacheBuilder.newUserManagedCacheBuilder(URI.class, byte[].class)
            .withResourcePools(ResourcePoolsBuilder.heap(entries)).build();
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() {
        val params = new ArrayList<Arguments>();
        val defaultPolicy = new ThresholdExpiredCRLRevocationPolicy(0);
        val zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy(0);

        /*
         * Test case #0
         * Valid certificate on valid CRL data with encoded url
         */
        var cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null),
            new String[]{"uservalid-encoded-crl.crt"},
            "test ca.crl",
            null
        ));

        /*
         * Test case #1
         * Valid certificate on valid CRL data
         */
        cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null, true),
            new String[]{"user-valid-distcrl.crt"},
            "userCA-valid.crl",
            null
        ));


        /* Test case #2
         * Revoked certificate on valid CRL data
         */
        cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null),
            new String[]{"user-revoked-distcrl.crt"},
            "userCA-valid.crl",
            new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), new BigInteger("1"))
        ));

        /* Test case #3
         * Valid certificate on expired CRL data
         */
        cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, zeroThresholdPolicy, null),
            new String[]{"user-valid-distcrl.crt"},
            "userCA-expired.crl",
            new ExpiredCRLException("test", ZonedDateTime.now(ZoneOffset.UTC))
        ));

        /* Test case #4
         * Valid certificate on expired CRL data with custom expiration
         * policy to always allow expired CRL data
         */
        cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, crl -> {
            }, null),
            new String[]{"user-valid-distcrl.crt"},
            "userCA-expired.crl",
            null
        ));

        /* Test case #5
         * Valid certificate with no CRL distribution points defined but with
         * "AllowRevocationPolicy" set to allow unavailable CRL data
         */
        cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, new AllowRevocationPolicy()),
            new String[]{"user-valid.crt"},
            "userCA-expired.crl",
            null
        ));

        /* Test case #6
         * EJBCA test case
         * Revoked certificate with CRL distribution point URI that is technically
         * not a valid URI since the issuer DN in the query string is not encoded per
         * the escaping of reserved characters in RFC 2396.
         * Make sure we can convert given URI to valid URI and confirm it's revoked
         */
        cache = getCache(100);
        params.add(arguments(
            new CRLDistributionPointRevocationChecker(cache, defaultPolicy, null),
            new String[]{"user-revoked-distcrl2.crt"},
            "userCA-valid.crl",
            new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), new BigInteger("1"))
        ));

        return params.stream();
    }

    @AfterAll
    public static void destroy() {
        val file = new File("ca.crl");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Called once before every test.
     */
    @AfterEach
    public void afterEachTest() {
        LOGGER.debug("Stopping web server...");
        this.webServer.stop();
        LOGGER.debug("Web server stopped [{}]", !this.webServer.isRunning());
    }
}
