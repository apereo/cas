/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.io.IOUtils;
import org.jasig.cas.adaptors.x509.util.MockWebServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * Unit test for {@link CRLDistributionPointRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.76
 *
 */
@RunWith(Parameterized.class)
public class CRLDistributionPointRevocationCheckerTests extends AbstractCRLRevocationCheckerTests {

    /** Instance under test. */
    private final CRLDistributionPointRevocationChecker checker;

    /** Answers requests for CRLs made to localhost:8085. */
    private final MockWebServer webServer;


    /**
     * Creates a new test instance with given parameters.
     *
     * @param checker Revocation checker instance.
     * @param expiredCRLPolicy Policy instance for handling expired CRL data.
     * @param certFiles File names of certificates to check.
     * @param crlFile File name of CRL file to serve out.
     * @param expected Expected result of check; null to indicate expected success.
     */
    public CRLDistributionPointRevocationCheckerTests(
            final CRLDistributionPointRevocationChecker checker,
            final RevocationPolicy<X509CRL> expiredCRLPolicy,
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
        this.checker.setExpiredCRLPolicy(expiredCRLPolicy);
        this.webServer = new MockWebServer(8085, new FileSystemResource(file), "text/plain");
        logger.debug("Web server listening on port 8085 serving file {}", crlFile);
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        final Collection<Object[]> params = new ArrayList<>();
        Cache cache;
        final ThresholdExpiredCRLRevocationPolicy defaultPolicy = new ThresholdExpiredCRLRevocationPolicy();
        final ThresholdExpiredCRLRevocationPolicy zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy();
        zeroThresholdPolicy.setThreshold(0);

        // Test case #0
        // Valid certificate on valid CRL data with encoded url
        cache = new Cache("crlCache-0", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
                new CRLDistributionPointRevocationChecker(cache),
                defaultPolicy,
                new String[]{"uservalid-encoded-crl.crt"},
                "test ca.crl",
                null,
        });

        // Test case #1
        // Valid certificate on valid CRL data
        cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[]{
                new CRLDistributionPointRevocationChecker(cache, true),
                defaultPolicy,
                new String[]{"user-valid-distcrl.crt"},
                "userCA-valid.crl",
                null,
        });


        // Test case #2
        // Revoked certificate on valid CRL data
        cache = new Cache("crlCache-2", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[] {
                new CRLDistributionPointRevocationChecker(cache),
                defaultPolicy,
                new String[] {"user-revoked-distcrl.crt"},
                "userCA-valid.crl",
                new RevokedCertificateException(new Date(), new BigInteger("1")),
        });

        // Test case #3
        // Valid certificate on expired CRL data
        cache = new Cache("crlCache-3", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[] {
                new CRLDistributionPointRevocationChecker(cache),
                zeroThresholdPolicy,
                new String[] {"user-valid-distcrl.crt"},
                "userCA-expired.crl",
                new ExpiredCRLException("test", new Date()),
        });

        // Test case #4
        // Valid certificate on expired CRL data with custom expiration
        // policy to always allow expired CRL data
        cache = new Cache("crlCache-4", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[] {
                new CRLDistributionPointRevocationChecker(cache),
                new RevocationPolicy<X509CRL>() {
                    @Override
                    public void apply(final X509CRL crl) {/* Do nothing to allow unconditionally */}
                },
                new String[] {"user-valid-distcrl.crt"},
                "userCA-expired.crl",
                null,
        });

        // Test case #5
        // Valid certificate with no CRL distribution points defined but with
        // "AllowRevocationPolicy" set to allow unavailable CRL data
        cache = new Cache("crlCache-5", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        final CRLDistributionPointRevocationChecker checker5 = new CRLDistributionPointRevocationChecker(cache);
        checker5.setUnavailableCRLPolicy(new AllowRevocationPolicy());
        params.add(new Object[] {
                checker5,
                defaultPolicy,
                new String[] {"user-valid.crt"},
                "userCA-expired.crl",
                null,
        });

        // Test case #6
        // EJBCA test case
        // Revoked certificate with CRL distribution point URI that is technically
        // not a valid URI since the issuer DN in the querystring is not encoded per
        // the escaping of reserved characters in RFC 2396.
        // Make sure we can convert given URI to valid URI and confirm it's revoked
        cache = new Cache("crlCache-6", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        params.add(new Object[] {
                new CRLDistributionPointRevocationChecker(cache),
                defaultPolicy,
                new String[] {"user-revoked-distcrl2.crt"},
                "userCA-valid.crl",
                new RevokedCertificateException(new Date(), new BigInteger("1")),
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
     * @throws Exception On setup errors.
     */
    @After
    public void tearDown() throws Exception {
        logger.debug("Stopping web server...");
        this.webServer.stop();
        Thread.sleep(500);
        logger.debug("Web server stopped [{}]", !this.webServer.isRunning());
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
