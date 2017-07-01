package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.ResourceCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Unit tests for {@link ResourceCRLRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@RunWith(Parameterized.class)
public class ResourceCRLRevocationCheckerTests extends AbstractCRLRevocationCheckerTests {
    /**
     * Instance under test.
     */
    private final ResourceCRLRevocationChecker checker;

    /**
     * Creates a new test instance with given parameters.
     *
     * @param checker          Revocation checker instance.
     * @param certFiles        File names of certificates to check.
     * @param expected         Expected result of check; null to indicate expected success.
     */
    public ResourceCRLRevocationCheckerTests(
            final ResourceCRLRevocationChecker checker,
            final String[] certFiles,
            final GeneralSecurityException expected) {

        super(certFiles, expected);

        this.checker = checker;
        try {
            this.checker.init();
        } catch (final Exception e) {
            throw new IllegalArgumentException("ResourceCRLRevocationChecker initialization failed", e);
        }
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {
        final Collection<Object[]> params = new ArrayList<>();

        final ThresholdExpiredCRLRevocationPolicy zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy(0);

        // Test case #1
        // Valid certificate on valid CRL data
        params.add(new Object[]{
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                        new ClassPathResource("userCA-valid.crl"),
                }, zeroThresholdPolicy),
            new String[]{"user-valid.crt"},
            null,
    });

        // Test case #2
        // Revoked certificate on valid CRL data
        params.add(new Object[]{
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                        new ClassPathResource("userCA-valid.crl"),
                        new ClassPathResource("intermediateCA-valid.crl"),
                        new ClassPathResource("rootCA-valid.crl"),
                }, zeroThresholdPolicy),
            new String[]{"user-revoked.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt"},
            new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), new BigInteger("1")),
        });

        // Test case #3
        // Valid certificate on expired CRL data for head cert
        params.add(new Object[]{
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                        new ClassPathResource("userCA-expired.crl"),
                        new ClassPathResource("intermediateCA-valid.crl"),
                        new ClassPathResource("rootCA-valid.crl"),
                }, zeroThresholdPolicy),
            new String[]{"user-valid.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt"},
            new ExpiredCRLException("test", ZonedDateTime.now(ZoneOffset.UTC)),
        });

        // Test case #4
        // Valid certificate on expired CRL data for intermediate cert
        params.add(new Object[]{
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                        new ClassPathResource("userCA-valid.crl"),
                        new ClassPathResource("intermediateCA-expired.crl"),
                        new ClassPathResource("rootCA-valid.crl"),
                }, zeroThresholdPolicy),
            new String[]{"user-valid.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt"},
            new ExpiredCRLException("test", ZonedDateTime.now(ZoneOffset.UTC)),
        });

        // Test case #5
        // Valid certificate on expired CRL data with custom expiration
        // policy to always allow expired CRL data
        params.add(new Object[]{
            new ResourceCRLRevocationChecker(new ClassPathResource[]{
                    new ClassPathResource("userCA-expired.crl"),
            }, crl -> {}),
            new String[]{"user-valid.crt"},
            null,
        });

        return params;
    }

    @Override
    protected RevocationChecker getChecker() {
        return this.checker;
    }
}
