package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.adaptors.x509.util.MockX509CRL;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.security.auth.x500.X500Principal;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Unit test for {@link ThresholdExpiredCRLRevocationPolicy} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.7
 */
@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class ThresholdExpiredCRLRevocationPolicyTests {
    /**
     * Policy instance under test.
     */
    private final ThresholdExpiredCRLRevocationPolicy policy;

    /**
     * CRL to test.
     */
    private final X509CRL crl;

    /**
     * Expected result of check; null for success
     */
    private final GeneralSecurityException expected;


    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {
        val params = new ArrayList<Object[]>();

        val now = ZonedDateTime.now(ZoneOffset.UTC);
        val twoHoursAgo = now.minusHours(2);
        val oneHourAgo = now.minusHours(1);
        val halfHourAgo = now.minusMinutes(30);
        val issuer = new X500Principal("CN=CAS");

        // Test case #1
        // Expect expired for zero leniency on CRL expiring 1ms ago
        val zeroThreshold = new ThresholdExpiredCRLRevocationPolicy(0);
        params.add(new Object[]{
            zeroThreshold,
            new MockX509CRL(issuer, DateTimeUtils.dateOf(oneHourAgo), DateTimeUtils.dateOf(now.minusSeconds(1))),
            new ExpiredCRLException("CN=CAS", ZonedDateTime.now(ZoneOffset.UTC)),
        });

        // Test case #2
        // Expect expired for 1h leniency on CRL expired 1 hour 1ms ago
        val oneHourThreshold = new ThresholdExpiredCRLRevocationPolicy(3600);
        params.add(new Object[]{
            oneHourThreshold,
            new MockX509CRL(issuer, DateTimeUtils.dateOf(twoHoursAgo), DateTimeUtils.dateOf(oneHourAgo.minusSeconds(1))),
            new ExpiredCRLException("CN=CAS", ZonedDateTime.now(ZoneOffset.UTC)),
        });

        // Test case #3
        // Expect valid for 1h leniency on CRL expired 30m ago
        params.add(new Object[]{
            oneHourThreshold,
            new MockX509CRL(issuer, DateTimeUtils.dateOf(twoHoursAgo), DateTimeUtils.dateOf(halfHourAgo)),
            null,
        });

        return params;
    }

    /**
     * Test method for {@link ThresholdExpiredCRLRevocationPolicy#apply(java.security.cert.X509CRL)}.
     */
    @Test
    public void verifyApply() {
        try {
            this.policy.apply(this.crl);
            if (this.expected != null) {
                Assert.fail("Expected exception of type " + this.expected.getClass());
            }
        } catch (final GeneralSecurityException e) {
            if (this.expected == null) {
                e.printStackTrace();
                Assert.fail("Revocation check failed unexpectedly with exception: " + e);
            } else {
                val expectedClass = this.expected.getClass();
                val actualClass = e.getClass();
                Assert.assertTrue(
                    String.format("Expected exception of type %s but got %s", expectedClass, actualClass),
                    expectedClass.isAssignableFrom(actualClass));
            }
        }
    }
}
