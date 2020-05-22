package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.ResourceCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;


/**
 * Unit tests for {@link ResourceCRLRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Tag("X509")
public class ResourceCRLRevocationCheckerTests extends BaseCRLRevocationCheckerTests {

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void checkCertificate(final ResourceCRLRevocationChecker checker, final String[] certFiles, final GeneralSecurityException expected) {
        checker.init();
        BaseCRLRevocationCheckerTests.checkCertificate(checker, certFiles, expected);
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() {
        val zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy(0);

        return Stream.of(
            /*
             * Test case #1
             * Valid certificate on valid CRL data
             */
            arguments(
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                    new ClassPathResource("userCA-valid.crl"),
                }, zeroThresholdPolicy),
                new String[]{"user-valid.crt"},
                null
            ),

            /*
             * Test case #2
             * Revoked certificate on valid CRL data
             */
            arguments(
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                    new ClassPathResource("userCA-valid.crl"),
                    new ClassPathResource("intermediateCA-valid.crl"),
                    new ClassPathResource("rootCA-valid.crl"),
                }, zeroThresholdPolicy),
                new String[]{"user-revoked.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt"},
                new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), new BigInteger("1"))
            ),

            /*
             * Test case #3
             * Valid certificate on expired CRL data for head cert
             */
            arguments(
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                    new ClassPathResource("userCA-expired.crl"),
                    new ClassPathResource("intermediateCA-valid.crl"),
                    new ClassPathResource("rootCA-valid.crl"),
                }, zeroThresholdPolicy),
                new String[]{"user-valid.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt"},
                new ExpiredCRLException("test", ZonedDateTime.now(ZoneOffset.UTC))
            ),

            /*
             * Test case #4: Valid certificate on expired CRL data for intermediate cert
             */
            arguments(
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                    new ClassPathResource("userCA-valid.crl"),
                    new ClassPathResource("intermediateCA-expired.crl"),
                    new ClassPathResource("rootCA-valid.crl"),
                }, zeroThresholdPolicy),
                new String[]{"user-valid.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt"},
                new ExpiredCRLException("test", ZonedDateTime.now(ZoneOffset.UTC))
            ),

            /*
             * Test case #5
             * Valid certificate on expired CRL data with custom expiration
             * policy to always allow expired CRL data
             */
            arguments(
                new ResourceCRLRevocationChecker(new ClassPathResource[]{
                    new ClassPathResource("userCA-expired.crl"),
                }, crl -> {
                }),
                new String[]{"user-valid.crt"},
                null
            )
        );
    }
}
