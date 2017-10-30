package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.ResourceCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.util.RegexUtils;
import org.cryptacular.util.CertUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for {@link X509CredentialsAuthenticationHandler} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
public class X509CredentialsAuthenticationHandlerTests {

    private static final String USER_VALID_CRT = "user-valid.crt";
    /**
     * Subject of test.
     */
    private final X509CredentialsAuthenticationHandler handler;

    /**
     * Test authentication credential.
     */
    private final Credential credential;

    /**
     * Expected result of supports test.
     */
    private final boolean expectedSupports;

    /**
     * Expected authentication result.
     */
    private final Object expectedResult;


    /**
     * Creates a new test class instance with the given parameters.
     *
     * @param handler    Test authentication handler.
     * @param credential Test credential.
     * @param supports   Expected result of supports test.
     * @param result     Expected result of authentication test.
     */
    public X509CredentialsAuthenticationHandlerTests(final X509CredentialsAuthenticationHandler handler, final Credential credential, final boolean supports,
                                                     final Object result) {

        this.handler = handler;
        this.credential = credential;
        this.expectedSupports = supports;
        this.expectedResult = result;
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {
        final Collection<Object[]> params = new ArrayList<>();

        X509CredentialsAuthenticationHandler handler;
        X509CertificateCredential credential;

        // Test case #1: Unsupported credential type
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"));
        params.add(new Object[]{handler, new UsernamePasswordCredential(), false, null});

        // Test case #2:Valid certificate
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"));
        credential = new X509CertificateCredential(createCertificates(USER_VALID_CRT));
        params.add(new Object[]{handler, credential, true, new DefaultHandlerResult(handler, credential,
                new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #3: Expired certificate
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"));
        params.add(new Object[]{
                handler,
            new X509CertificateCredential(createCertificates("user-expired.crt")),
            true,
            new CertificateExpiredException(),
    });

        // Test case #4: Untrusted issuer
        handler = new X509CredentialsAuthenticationHandler(
                RegexUtils.createPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US"),
                true, false, false);
        params.add(new Object[]{handler, new X509CertificateCredential(createCertificates("snake-oil.crt")),
                true, new FailedLoginException(),
        });

        // Test case #5: Disallowed subject
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
                true,
                RegexUtils.createPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US"));
        params.add(new Object[]{
                handler,
            new X509CertificateCredential(createCertificates("snake-oil.crt")),
            true,
            new FailedLoginException(),
    });

        // Test case #6: Check key usage on a cert without keyUsage extension
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
                false, true, false);
        credential = new X509CertificateCredential(createCertificates(USER_VALID_CRT));
        params.add(new Object[]{
                handler,
            credential,
            true,
            new DefaultHandlerResult(handler, credential, new DefaultPrincipalFactory().createPrincipal(credential.getId())),
    });

        // Test case #7: Require key usage on a cert without keyUsage extension
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
                false, true, true);
        params.add(new Object[]{
            handler,
            new X509CertificateCredential(createCertificates(USER_VALID_CRT)),
            true, new FailedLoginException(),
        });

        // Test case #8: Require key usage on a cert with acceptable keyUsage extension values
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
                false, true, true);
        credential = new X509CertificateCredential(createCertificates("user-valid-keyUsage.crt"));
        params.add(new Object[]{
                handler,
                credential,
                true,
                new DefaultHandlerResult(handler, credential, new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #9: Require key usage on a cert with unacceptable keyUsage extension values
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
                false, true, true);
        params.add(new Object[]{
                handler,
                new X509CertificateCredential(createCertificates("user-invalid-keyUsage.crt")),
                true,
                new FailedLoginException(),
        });

        //===================================
        // Revocation tests
        //===================================
        ResourceCRLRevocationChecker checker;

        // Test case #10: Valid certificate with CRL checking

        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
        checker.init();
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"), checker);
        credential = new X509CertificateCredential(createCertificates(USER_VALID_CRT));
        params.add(new Object[]{
                handler,
                new X509CertificateCredential(createCertificates(USER_VALID_CRT)),
                true,
                new DefaultHandlerResult(handler, credential, new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #11: Revoked end user certificate
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
        checker.init();
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"), checker);
        params.add(new Object[]{
                handler,
                new X509CertificateCredential(createCertificates("user-revoked.crt")),
                true,
                new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), null),
        });

        // Test case #12: Valid certificate on expired CRL data
        final ThresholdExpiredCRLRevocationPolicy zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy(0);
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-expired.crl"), null, zeroThresholdPolicy);
        checker.init();
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"), checker);
        params.add(new Object[]{
                handler,
                new X509CertificateCredential(createCertificates(USER_VALID_CRT)),
                true,
                new ExpiredCRLException(null, ZonedDateTime.now(ZoneOffset.UTC)),
        });

        return params;
    }

    /**
     * Tests the {@link X509CredentialsAuthenticationHandler#authenticate(Credential)} method.
     */
    @Test
    public void verifyAuthenticate() {
        try {
            if (this.handler.supports(this.credential)) {
                final HandlerResult result = this.handler.authenticate(this.credential);
                if (this.expectedResult instanceof DefaultHandlerResult) {
                    assertEquals(this.expectedResult, result);
                } else {
                    fail("Authentication succeeded when it should have failed with " + this.expectedResult);
                }
            }
        } catch (final Exception e) {
            if (this.expectedResult instanceof Exception) {
                assertEquals(this.expectedResult.getClass(), e.getClass());
            } else {
                fail("Authentication failed when it should have succeeded: " + e.getMessage());
            }
        }
    }

    /**
     * Tests the {@link X509CredentialsAuthenticationHandler#supports(Credential)} method.
     */
    @Test
    public void verifySupports() {
        assertEquals(this.expectedSupports, this.handler.supports(this.credential));
    }

    protected static X509Certificate[] createCertificates(final String... files) {
        final X509Certificate[] certs = new X509Certificate[files.length];

        int i = 0;
        for (final String file : files) {
            try {
                certs[i++] = CertUtil.readCertificate(new ClassPathResource(file).getInputStream());
            } catch (final Exception e) {
                throw new IllegalArgumentException("Error creating certificate at " + file, e);
            }
        }
        return certs;
    }
}

