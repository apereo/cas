package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.adaptors.x509.util.MockX509CRL;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.security.auth.x500.X500Principal;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.apereo.cas.util.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

/**
 * Unit test for {@link ThresholdExpiredCRLRevocationPolicy} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.7
 */
@RequiredArgsConstructor
public class ThresholdExpiredCRLRevocationPolicyTests {
    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        val twoHoursAgo = now.minusHours(2);
        val oneHourAgo = now.minusHours(1);
        val halfHourAgo = now.minusMinutes(30);
        val issuer = new X500Principal("CN=CAS");

        return Stream.of(
            // Test case #1
            // Expect expired for zero leniency on CRL expiring 1ms ago
            arguments(
                new ThresholdExpiredCRLRevocationPolicy(0),
                new MockX509CRL(issuer, DateTimeUtils.dateOf(oneHourAgo), DateTimeUtils.dateOf(now.minusSeconds(1))),
                new ExpiredCRLException("CN=CAS", ZonedDateTime.now(ZoneOffset.UTC))
            ),

            // Test case #2
            // Expect expired for 1h leniency on CRL expired 1 hour 1ms ago
            arguments(
                new ThresholdExpiredCRLRevocationPolicy(3600),
                new MockX509CRL(issuer, DateTimeUtils.dateOf(twoHoursAgo), DateTimeUtils.dateOf(oneHourAgo.minusSeconds(1))),
                new ExpiredCRLException("CN=CAS", ZonedDateTime.now(ZoneOffset.UTC))
            ),

            // Test case #3
            // Expect valid for 1h leniency on CRL expired 30m ago
            arguments(
                new ThresholdExpiredCRLRevocationPolicy(3600),
                new MockX509CRL(issuer, DateTimeUtils.dateOf(twoHoursAgo), DateTimeUtils.dateOf(halfHourAgo)),
                null
            )
        );
    }

    /**
     * Test method for {@link ThresholdExpiredCRLRevocationPolicy#apply(java.security.cert.X509CRL)}.
     */
    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyApply(final ThresholdExpiredCRLRevocationPolicy policy, final X509CRL crl, final GeneralSecurityException expected) {
        assertThrowsOrNot(expected, () -> policy.apply(crl));
    }
}
